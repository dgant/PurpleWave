package Micro.Decisions

import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.SpecificPoints
import Mathematics.PurpleMath
import Micro.Agency.OldExplosion
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
  
  /////////////////////
  //                 //
  // Specific fields //
  //                 //
  /////////////////////
  
  /////////////
  // Threats //
  /////////////
  
  def threatsRepulsion(unit: FriendlyUnitInfo): Force = {
    val threats     = unit.matchups.threats.filterNot(_.is(Protoss.Interceptor))
    val forces      = threats.map(threatRepulsion(unit, _))
    val output      = ForceMath.sum(forces).normalize
    output
  }
  
  protected def threatRepulsion(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val magnitudeDamage   = threat.dpfOnNextHitAgainst(unit)
    val magnitudeDistance = Math.max(1.0, 12.0 + threat.framesToGetInRange(unit))
    val magnitudeFinal    = magnitudeDamage / magnitudeDistance
    val output            = unitAttraction(unit, threat, -magnitudeFinal)
    output
  }
  
  //////////
  // Team //
  //////////
  
  def teamAttraction(unit: FriendlyUnitInfo): Force = {
    val allies                = (unit.matchups.allies ++ unit.squad.map(_.recruits).getOrElse(List.empty)).filter(_ != unit)
    val alliesUseful          = allies.filter(ally => unit.matchups.threats.exists(ally.canAttack))
    val allyNearestUseful     = ByOption.minBy(alliesUseful)(ally => ally.pixelDistanceFast(unit) - ally.effectiveRangePixels)
    if (allyNearestUseful.isEmpty) return new Force
    val allyDistance          = allyNearestUseful.get.pixelDistanceFast(unit)
    val allyDistanceAlarming  = Math.max(32.0 * 3.0, allyNearestUseful.get.effectiveRangePixels)
    val magnitude             = Math.min(1.0, allyDistance / allyDistanceAlarming)
    val output                = unitAttraction(unit, allyNearestUseful.get, magnitude)
    output
  }
  
  ////////////////
  // Explosions //
  ////////////////
  
  def explosionRepulsion(unit: FriendlyUnitInfo, explosion: OldExplosion): Force = {
    val magnitudeDamage   = explosion.damage
    val magnitudeDistance = explosion.safetyRadius / Math.max(1.0, unit.pixelDistanceFast(explosion.pixelCenter))
    val magnitudeFinal    = magnitudeDamage / magnitudeDistance
    val output            = ForceMath.fromPixels(unit.pixelCenter, explosion.pixelCenter, -magnitudeFinal)
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
    val output = unitAttraction(unit, detector, -1.0 / detector.pixelDistanceFast(unit))
    output
  }
  
  //////////////
  // Mobility //
  //////////////

  def mobilityAttraction(unit: FriendlyUnitInfo): Force = {
    val bestAdjacent  = ByOption.max(unit.tileIncludingCenter.adjacent8.filter(_.valid).map(unit.mobilityGrid.get)).getOrElse(0)
    val magnitudeNeed = 1.0 / Math.max(1.0, unit.mobility - 1.0)
    val magnitudeCap  = PurpleMath.nanToZero(bestAdjacent.toDouble / unit.mobility)
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
  
  def collisionRepulsion(unit: FriendlyUnitInfo): Force = {
    if (unit.flying && ! unit.matchups.threats.exists(_.unitClass.dealsRadialSplashDamage)) return new Force
    
    val blockers        = unit.matchups.allies.filterNot(_.flying)
    val nearestBlocker  = ByOption.minBy(blockers)(_.pixelsFromEdgeFast(unit))
    
    if (nearestBlocker.isEmpty) return new Force
    
    val maximumDistance = Math.max(32.0, unit.pixelRangeMax)
    val blockerDistance = nearestBlocker.get.pixelsFromEdgeFast(unit)
    val magnitude       = Math.max(0.0, 1.0 - blockerDistance / maximumDistance)
    val output          = unitAttraction(unit, nearestBlocker.get, - magnitude)
    output
  }
  
  /////////////
  // Exiting //
  /////////////
  
  def exitAttraction(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) exitAttractionAir(unit) else exitAttractionGround(unit)
  }
  
  protected def exitAttractionAir(unit: FriendlyUnitInfo): Force = {
    val zoneNow       = unit.zone
    val zoneOrigin    = unit.agent.origin.zone
    val origin        = if (zoneNow == zoneOrigin) SpecificPoints.middle else unit.agent.origin
    
    ForceMath.fromPixels(unit.pixelCenter, origin).normalize
  }
  
  protected def exitAttractionGround(unit: FriendlyUnitInfo): Force = {
    val path          = unit.agent.zonePath(unit.agent.origin)
    val origin        = if (path.isEmpty || path.get.steps.isEmpty) unit.agent.origin.zone.centroid.pixelCenter else path.get.steps.head.edge.centerPixel
    val forceExiting  = ForceMath.fromPixels(unit.pixelCenter, origin)
    val forceNormal   = forceExiting.normalize
    forceNormal
  }
  
  ///////////////
  // Smuggling //
  ///////////////
  
  def smuggleRepulsion(unit: FriendlyUnitInfo): Force = {
    if (unit.tileIncludingCenter.tileDistanceFromEdge < 5) return new Force
    -unit.mobilityForce.normalize
  }
}
