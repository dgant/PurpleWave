package Micro.Decisions

import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Potential {
  
  //////////////////////////////////////////////
  //                                          //
  // Generic. Build your own potential field! //
  //                                          //
  //////////////////////////////////////////////
  
  def unitAttraction(unit: FriendlyUnitInfo, other: UnitInfo, magnifier: (UnitInfo, UnitInfo) => Double): Force = {
    unitAttraction(unit, other, magnifier(unit, other))
  }
  
  def unitAttraction(unit: FriendlyUnitInfo, other: UnitInfo, magnitude: Double): Force = {
    val pixelFrom = unit.pixelCenter
    val pixelTo   = other.pixelCenter
    val output    = ForceMath.fromPixels(pixelFrom, pixelTo, magnitude)
    output
  }
  
  def unitResistance(unit: FriendlyUnitInfo, pixelFrom: Pixel, range: Double): Force = {
    val pixelTo   = unit.pixelCenter
    val distance  = unit.pixelDistanceEdge(pixelFrom)
    val magnitude = Math.max(0, range - distance) / range
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
    val threats = unit.matchups.threats.filterNot(_.is(Protoss.Interceptor))
    val forces  = threats.map(threatRepulsion(unit, _))
    val output  = ForceMath.sum(forces).normalize
    output
  }
  
  protected def threatRepulsion(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val entanglement          = unit.matchups.framesOfEntanglementPerThreat.getOrElse(threat, threat.framesToGetInRange(unit).toDouble)
    val magnitudeEntanglement = 1.0 + PurpleMath.fastTanh(entanglement/24.0) + Math.max(0.0, entanglement/24.0)
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
    val allies                = (unit.matchups.allies ++ unit.squad.map(_.units).getOrElse(List.empty)).filter(_ != unit)
    val alliesUseful          = allies.filter(ally => unit.matchups.threats.exists(ally.canAttack))
    val allyNearestUseful     = ByOption.minBy(alliesUseful)(ally => ally.pixelDistanceCenter(unit) - ally.effectiveRangePixels)
    if (allyNearestUseful.isEmpty) return new Force
    val allyDistance          = allyNearestUseful.get.pixelDistanceCenter(unit)
    val allyDistanceAlarming  = if (unit.flying) 1.0 else Math.max(32.0 * 2.0, Math.max(unit.effectiveRangePixels, allyNearestUseful.get.effectiveRangePixels))
    val magnitude             = Math.min(1.0, PurpleMath.nanToOne(allyDistance / allyDistanceAlarming))
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
    
    val splashThreats = unit.matchups.threats.filter(_.unitClass.dealsRadialSplashDamage)
    if (splashThreats.isEmpty) return new Force
    
    val splashRadius: Double = ByOption.max(splashThreats.map(_.unitClass.airSplashRadius25.toDouble)).getOrElse(0.0)
    if (splashRadius <= 0) return new Force
  
    val splashAllies = unit.matchups.allies.filter(ally =>
      ! ally.unitClass.isBuilding
      && (ally.flying == unit.flying || splashThreats.take(3).exists(_.canAttack(ally))))
    if (splashAllies.isEmpty) return new Force
  
    def splashRepulsionMagnitude(self: UnitInfo, ally: UnitInfo): Double = {
      val denominator = splashRadius + self.unitClass.radialHypotenuse
      val numerator   = - Math.max(0.0, denominator - self.pixelDistanceEdge(ally))
      val output      = numerator / denominator
      output
    }
    
    val forces  = splashAllies.map(Potential.unitAttraction(unit, _, magnifier = splashRepulsionMagnitude))
    val output  = ForceMath.sum(forces).normalize(forces.map(_.lengthFast).max)
    output
  }
  
  ///////////////
  // Detection //
  ///////////////
  
  def detectionRepulsion(unit: FriendlyUnitInfo): Force = {
    val detectors   = unit.matchups.enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
    val forces      = detectors.map(detectorRepulsion(unit, _))
    val output      = ForceMath.sum(forces).normalize
    output
  }
  
  protected def detectorRepulsion(unit: FriendlyUnitInfo, detector: UnitInfo): Force = {
    val output = unitAttraction(unit, detector, -1.0 / detector.pixelDistanceCenter(unit))
    output
  }
  
  //////////////
  // Mobility //
  //////////////
  
  def resistTerrain(unit: FriendlyUnitInfo): Vector[Force] = {
    if (unit.flying) airResistances(unit) else groundResistances(unit)
  }
  
  def airResistances(unit: FriendlyUnitInfo): Vector[Force] = {
    val boundary = 32.0 * 3.0
    val output = Vector(
      unitResistance(unit, Pixel(0,                   unit.y),              boundary),
      unitResistance(unit, Pixel(With.mapPixelWidth,  unit.y),              boundary),
      unitResistance(unit, Pixel(unit.x,              0),                   boundary),
      unitResistance(unit, Pixel(unit.x,              With.mapPixelHeight), boundary)
    )
    .filter(_.lengthSquared > 0)
    output
  }
  
  def groundResistances(unit: FriendlyUnitInfo): Vector[Force] = {
    unit
      .tileIncludingCenter
      .toRectangle
      .expand(2, 2)
      .tiles
      .filter(tile => ! tile.valid || ! With.grids.walkable.get(tile))
      .map(tile =>
        unitResistance(
          unit,
          // Want to push against the unit's edge, but beware that units can sometimes stand on unwalkable tiles,
          // especially buildings/terrain that don't fill the tile completely.
          unit.pixelCenter.project(tile.pixelCenter, 1.0 + unit.pixelDistanceEdge(tile.pixelCenter)),
          48.0))
      .filter(_.lengthSquared > 0)
      .toVector
  }

  def preferMobility(unit: FriendlyUnitInfo): Force = {
    //if (Gravitate.useShortAreaPathfinding(unit)) return new Force
    val bestAdjacent  = ByOption.max(unit.tileIncludingCenter.adjacent8.filter(_.valid).map(unit.mobilityGrid.get)).getOrElse(0)
    val magnitudeNeed = 1.0 / Math.max(1.0, unit.mobility - 1.0)
    val magnitudeCap  = (1.0 + bestAdjacent.toDouble) / (1.0 + unit.mobility)
    val magnitude     = Math.min(magnitudeNeed, magnitudeCap)
    val output        = unit.mobilityForce.normalize(magnitude)
    output
  }
  
  def cliffAttraction(unit: FriendlyUnitInfo): Force = {
    With.grids.mobilityForceGround.get(unit.tileIncludingCenter).normalize(-1.0)
  }
  
  ////////////////
  // Collisions //
  ////////////////
  
  def collisionResistances(unit: FriendlyUnitInfo): Vector[Force] = {
    if (unit.flying) return Vector.empty
    unit.matchups.others
      .filter(u => ! u.unitClass.isBuilding && ! u.flying)
      .map(neighbor => unitResistance(unit, neighbor.pixelCenter, 2.0 * unit.unitClass.radialHypotenuse))
      .filter(_.lengthSquared > 0)
  }
  
  def collisionRepulsion(unit: FriendlyUnitInfo, other: UnitInfo): Force = {
    if (unit.flying) return new Force
    if (other.flying) return new Force
    if (other.unitClass.isBuilding) return new Force
  
    val maximumDistance   = 40
    val blockerDistance   = other.pixelDistanceEdge(unit)
    val magnitudeDistance = 1.0 - PurpleMath.clampToOne(blockerDistance / (1.0 + maximumDistance))
    val magnitudeSize     = unit.unitClass.dimensionMax * other.unitClass.dimensionMax / 32.0 / 32.0
    val magnitude         = magnitudeSize * magnitudeDistance
    unitAttraction(unit, other, -magnitude)
  }
  
  def avoidCollision(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    val repulsions = unit.matchups.others.filterNot(o => o.flying || o.unitClass.isBuilding).map(collisionRepulsion(unit, _))
    ByOption.maxBy(repulsions)(_.lengthSquared).getOrElse(new Force)
  }
  
  /////////////
  // Exiting //
  /////////////
  
  def preferTravelling(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) travelAttractionAir(unit) else travelAttractionGround(unit)
  }
  
  protected def travelAttractionAir(unit: FriendlyUnitInfo): Force = {
    val zoneNow   = unit.zone
    val zoneTo    = unit.agent.destination.zone
    val origin    = if (zoneNow == zoneTo) SpecificPoints.middle else unit.agent.origin
    
    ForceMath.fromPixels(unit.pixelCenter, origin).normalize
  }
  
  protected def travelAttractionGround(unit: FriendlyUnitInfo): Force = {
    val path          = unit.agent.zonePath(unit.agent.destination)
    val destination   = if (path.isEmpty || path.get.steps.isEmpty) unit.agent.destination.zone.centroid.pixelCenter else path.get.steps.head.edge.pixelCenter
    val forceExiting  = ForceMath.fromPixels(unit.pixelCenter, destination, 1.0)
    forceExiting
  }
  
  ///////////////
  // Smuggling //
  ///////////////
  
  def smuggleRepulsion(unit: FriendlyUnitInfo): Force = {
    if (unit.tileIncludingCenter.tileDistanceFromEdge < 5) return new Force
    -unit.mobilityForce.normalize
  }
}

