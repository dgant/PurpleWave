package Micro.Decisions

import Lifecycle.With
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.PurpleMath
import Micro.Agency.Explosion
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
  
  ////////////////
  // Explosions //
  ////////////////
  
  def explosionsRepulsion(unit: FriendlyUnitInfo): Force = {
    val explosions  = unit.agent.explosions
    val forces      = explosions.map(explosionRepulsion(unit, _))
    val output      = ForceMath.sum(forces).normalize
    output
  }
  
  def explosionRepulsion(unit: FriendlyUnitInfo, explosion: Explosion): Force = {
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
    
    // When we're in tight spaces (like against a mineral line) this isn't going to help us.
    if (unit.tileIncludingCenter.adjacent8.filter(_.valid).forall(unit.mobilityGrid.get(_) <= 1)) return new Force
    
    val mobilityNeed  = 3.0 + ByOption.max(unit.matchups.threatsViolent.map(threat => 2 * threat.pixelRangeAgainstFromCenter(unit) / 32)).getOrElse(0.0)
    val mobilityNow   = unit.mobility
    val mobilityCap   = if (unit.flying) 12 else unit.zone.maxMobility / 2.0
    val mobilityForce = unit.mobilityForce
    val magnitudeRaw  = Math.min(mobilityNeed, mobilityCap) / mobilityNow / 2.0
    val magnitude     = PurpleMath.clamp(magnitudeRaw, 0.0, 5.0)
    val output        = mobilityForce.normalize(magnitude)
    output
  }
  
  ////////////////
  // Collisions //
  ////////////////
  
  def collisionRepulsion(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    
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
    ForceMath.fromPixels(unit.pixelCenter, unit.agent.origin).normalize
  }
  
  protected def exitAttractionGround(unit: FriendlyUnitInfo): Force = {
    val zoneNow       = unit.zone
    val zoneOrigin    = unit.agent.origin.zone
    val path          = With.paths.zonePath(zoneNow, zoneOrigin)
    
    if (path.isEmpty || path.get.steps.isEmpty) return new Force
    
    val forceExiting  = ForceMath.fromPixels(unit.pixelCenter, path.get.steps.head.edge.centerPixel)
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
