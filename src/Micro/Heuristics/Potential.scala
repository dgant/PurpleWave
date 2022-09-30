package Micro.Heuristics

import Mathematics.Maff
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Potential {

  def towards(unit: FriendlyUnitInfo, goal: Pixel): Force = {
    ForceMath.fromPixels(unit.pixel, MicroPathing.getWaypointToPixel(unit, goal))
  }

  def towardsUnitCustom(unit: FriendlyUnitInfo, other: UnitInfo, magnifier: (UnitInfo, UnitInfo) => Double = (a, b) => 1.0): Force = {
    towardsUnit(unit, other, magnifier(unit, other))
  }

  def towardsUnit(unit: FriendlyUnitInfo, other: UnitInfo, magnitude: Double = 1.0): Force = {
    ForceMath.fromPixels(unit.pixel, other.pixel.nearestTraversablePixel(unit), magnitude)
  }

  def towardsTarget(unit: FriendlyUnitInfo): Force = {
    val target    = unit.agent.toAttack.map(unit.pixelToFireAt).getOrElse(unit.agent.destination)
    val distance  = unit.pixelDistanceTravelling(target)
    towards(unit, target).normalize(Maff.clamp(Maff.nanToInfinity(32.0 / distance), 0.0, 1.0))
  }

  def hardAvoidEdge(unit: FriendlyUnitInfo, other: UnitInfo, minDistance: Double, equidistance: Double): Force = {
    ForceMath.fromPixels(other.pixel, unit.pixel, hardScale(unit.pixelDistanceEdge(other), minDistance, equidistance))
  }

  def hardAvoidEntanglement(unit: FriendlyUnitInfo, other: UnitInfo, minDistance: Double, equidistance: Double): Force = {
    ForceMath.fromPixels(other.pixel, unit.pixel, hardScale( - unit.pixelsOfEntanglement(other), minDistance, equidistance))
  }

  private def hardScale(distance: Double, minDistance: Double, equidistance: Double, cap: Double = 1e6): Double = {
         if (distance >= equidistance)  equidistance / distance
    else if (distance >  minDistance)   Math.min(cap, (equidistance - minDistance) / (distance - minDistance))
    else                                cap
  }

  /////////////
  // Threats //
  /////////////

  def softAvoidThreatRange(unit: FriendlyUnitInfo): Force = {
    ForceMath.sum(unit.matchups.threats.map(threatRepulsion(unit, _))).normalize
  }
  protected def threatRepulsion(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val entanglement          = unit.pixelsOfEntanglement(threat)
    val magnitudeEntanglement = 1.0 + Maff.fastTanh11(entanglement / 32.0) + Math.max(0.0, entanglement / 32.0)
    val magnitudeDamage       = threat.dpfOnNextHitAgainst(unit)
    val magnitudeFinal        = magnitudeDamage * magnitudeEntanglement
    val output                = towardsUnit(unit, threat, -magnitudeFinal)
    output
  }

  def hardAvoidThreatRange(unit: FriendlyUnitInfo, margin: Double = 32.0): Force = {
    Maff.maxBy(unit.matchups.threats.map(hardAvoidEntanglement(unit, _, 0.0, margin)))(_.lengthFast).getOrElse(new Force())
  }

  ////////////
  // Splash //
  ////////////

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
    val forces  = splashAllies.map(towardsUnitCustom(unit, _, splashRepulsionMagnitude))
    val output  = ForceMath.sum(forces).normalize(forces.map(_.lengthFast).max)
    output
  }

  ///////////////
  // Detection //
  ///////////////

  def detectionRepulsion(unit: FriendlyUnitInfo): Force = {
    if ( ! unit.cloaked) return new Force
    val detectors   = unit.matchups.enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
    val forces      = detectors.map(detector => towardsUnit(unit, detector, -1.0 / detector.pixelDistanceCenter(unit)))
    val output      = ForceMath.sum(forces).normalize
    output
  }

  ////////////////
  // Collisions //
  ////////////////

  private val tts = 1d / 32d / 32d
  private def collisionRepulsionMagnitude(unit: FriendlyUnitInfo, other: UnitInfo, margin: Double = 8.0): Double = {
    if (unit.flying) return 0.0
    if (other.flying) return 0.0
    if (other.velocity.lengthSquared < 0.01) return 0.0
    if (other.topSpeed >= unit.topSpeed && other.speedApproaching(unit) < 0) return 0.0
    val maximumDistance   = Math.max(unit.unitClass.dimensionMax, other.unitClass.dimensionMax)
    val blockerDistance   = other.pixelDistanceEdge(unit)
    val magnitudeDistance = 1.0 - Maff.clamp(blockerDistance / (1.0 + maximumDistance), 0.0, 1.0)
    val magnitudeSize     = unit.unitClass.dimensionMax * other.unitClass.dimensionMax * tts
    val output            = magnitudeSize * magnitudeDistance
    output
  }

  def collisionRepulsion(unit: FriendlyUnitInfo, other: UnitInfo): Force = {
    val magnitude = collisionRepulsionMagnitude(unit, other)
    if (magnitude == 0) new Force else towardsUnit(unit, other, -magnitude)
  }
  
  def avoidCollision(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    val repulsions = unit.inTileRadius(3).map(collisionRepulsion(unit, _))
    ForceMath.mean(repulsions).clipMin(1.0)
  }
}

