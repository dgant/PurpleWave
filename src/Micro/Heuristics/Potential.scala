package Micro.Heuristics

import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Mathematics.Maff
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}


object Potential {
  
  def unitAttraction(unit: FriendlyUnitInfo, other: UnitInfo, magnifier: (UnitInfo, UnitInfo) => Double): Force = {
    unitAttraction(unit, other, magnifier(unit, other))
  }
  
  def unitAttraction(unit: FriendlyUnitInfo, other: UnitInfo, magnitude: Double): Force = {
    val pixelFrom = unit.pixel
    val pixelTo   = other.pixel
    val output    = ForceMath.fromPixels(pixelFrom, pixelTo, magnitude)
    output
  }
  
  /////////////////////
  //                 //
  // Specific fields //
  //                 //
  /////////////////////
  
  /////////////
  // Threats //
  /////////////

  def avoidThreatsWhileCloaked(unit: FriendlyUnitInfo): Force = {
    val threats = unit.matchups.enemies.filter(e => if (unit.flying) e.unitClass.attacksAir else e.unitClass.attacksGround)
    val forces  = threats.map(threatRepulsion(unit, _))
    val output  = ForceMath.sum(forces).normalize
    output
  }

  def avoidThreats(unit: FriendlyUnitInfo): Force = {
    val threats = unit.matchups.threats.filterNot(Protoss.Interceptor)
    val forces  = threats.map(threatRepulsion(unit, _))
    val output  = ForceMath.sum(forces).normalize
    output
  }
  
  protected def threatRepulsion(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val entanglement          = unit.pixelsOfEntanglement(threat)
    val magnitudeEntanglement = 1.0 + Maff.fastTanh(entanglement / 32.0) + Math.max(0.0, entanglement / 32.0)
    val magnitudeDamage       = threat.dpfOnNextHitAgainst(unit)
    val magnitudeFinal        = magnitudeDamage * magnitudeEntanglement
    val output                = unitAttraction(unit, threat, -magnitudeFinal)
    output
  }
  
  //////////
  // Team //
  //////////

  def preferRegrouping(unit: FriendlyUnitInfo): Force = {
    if (unit.base.exists(_.owner.isUs)) return new Force
    val allies                = unit.alliesBattleOrSquad.view.filter(_ != unit)
    val alliesUseful          = allies.filter(ally => unit.matchups.threats.exists(ally.canAttack))
    val allyNearestUseful     = Maff.minBy(alliesUseful)(ally => ally.pixelDistanceCenter(unit) - ally.effectiveRangePixels)
    if (allyNearestUseful.isEmpty) return new Force
    val allyDistance          = allyNearestUseful.get.pixelDistanceCenter(unit)
    val allyDistanceAlarming  = if (unit.flying) 1.0 else Math.max(32.0 * 2.0, Math.max(unit.effectiveRangePixels, allyNearestUseful.get.effectiveRangePixels))
    val magnitude             = Math.min(1.0, Maff.nanToOne(allyDistance / allyDistanceAlarming))
    val output                = unitAttraction(unit, allyNearestUseful.get, magnitude)
    output
  }
  
  ////////////
  // Splash //
  ////////////
  
  protected def splashRepulsionMagnitude(splashRadius: Double): (UnitInfo, UnitInfo) => Double = {
    (self, ally) => {
      val denominator = splashRadius
      val numerator = - Math.max(0.0, denominator - self.pixelDistanceEdge(ally))
      numerator / denominator
    }
  }
  
  def preferSpreading(unit: FriendlyUnitInfo): Force = {
    lazy val splashThreats = unit.matchups.threats.filter(_.unitClass.dealsRadialSplashDamage)
    lazy val splashRadius: Double = Maff.max(splashThreats.map(_.unitClass.airSplashRadius25.toDouble)).getOrElse(0.0)
    lazy val splashAllies = unit.alliesBattle.filter(ally =>
      ! ally.unitClass.isBuilding
      && (ally.flying == unit.flying || splashThreats.take(3).exists(_.canAttack(ally))))
    if (splashThreats.isEmpty) return new Force
    if (splashRadius <= 0) return new Force
    if (splashAllies.isEmpty) return new Force
    def splashRepulsionMagnitude(self: UnitInfo, ally: UnitInfo): Double = {
      val denominator = splashRadius + self.unitClass.radialHypotenuse
      val numerator   = - Math.max(0.0, denominator - self.pixelDistanceEdge(ally))
      val output      = numerator / denominator
      output
    }
    val forces  = splashAllies.map(unitAttraction(unit, _, splashRepulsionMagnitude(_, _)))
    val output  = ForceMath.sum(forces).normalize(forces.map(_.lengthFast).max)
    output
  }

  ///////////////
  // Detection //
  ///////////////

  def detectionRepulsion(unit: FriendlyUnitInfo): Force = {
    if ( ! unit.cloaked) return new Force
    val detectors   = unit.matchups.enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
    val forces      = detectors.map(detector => unitAttraction(unit, detector, -1.0 / detector.pixelDistanceCenter(unit)))
    val output      = ForceMath.sum(forces).normalize
    output
  }
  
  ////////////////
  // Collisions //
  ////////////////

  def preferTravel(unit: FriendlyUnitInfo, goal: Pixel): Force = {
    ForceMath.fromPixels(unit.pixel, MicroPathing.getWaypointToPixel(unit, goal), 1.0)
  }

  private val tts = 1d / 32d / 32d
  private def collisionRepulsionMagnitude(unit: FriendlyUnitInfo, other: UnitInfo): Double = {
    if (unit.flying) return 0.0
    if (other.flying) return 0.0
    if (other.unitClass.isBuilding) return 0.0
    val maximumDistance   = Math.max(unit.unitClass.dimensionMax, other.unitClass.dimensionMax)
    val blockerDistance   = other.pixelDistanceEdge(unit)
    val magnitudeDistance = 1.0 - Maff.clamp(blockerDistance / (1.0 + maximumDistance), 0.0, 1.0)
    val magnitudeSize     = unit.unitClass.dimensionMax * other.unitClass.dimensionMax * tts
    val output            = magnitudeSize * magnitudeDistance
    output
  }
  
  def collisionRepulsion(unit: FriendlyUnitInfo, other: UnitInfo): Force = {
    val magnitude = collisionRepulsionMagnitude(unit, other)
    if (magnitude == 0) new Force else unitAttraction(unit, other, -magnitude)
  }
  
  def avoidCollision(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    val repulsions = unit.inTileRadius(3).map(collisionRepulsion(unit, _))
    val output = ForceMath.mean(repulsions).clipMin(1.0)
    output
  }
}

