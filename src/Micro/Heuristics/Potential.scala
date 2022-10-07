package Micro.Heuristics

import Mathematics.Maff
import Mathematics.Physics.{Force, ForceMath}
import Mathematics.Points.Pixel
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

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

  def towardsDestination(unit: FriendlyUnitInfo): Force = {
    towards(unit, unit.agent.destination)
  }

  def towardsTarget(unit: FriendlyUnitInfo): Force = {
    val target    = unit.agent.toAttack.map(unit.pixelToFireAt).getOrElse(unit.agent.destination)
    val distance  = unit.pixelDistanceTravelling(target)
    towards(unit, target).normalize(Maff.clamp(Maff.nanToInfinity(32.0 / distance), 0.0, 1.0))
  }

  def hardAvoid(unit: FriendlyUnitInfo, pixel: Pixel, minDistance: Double, equidistance: Double): Force = {
    ForceMath.fromPixels(pixel, unit.pixel, hardScale(unit.pixelDistanceCenter(pixel), minDistance, equidistance))
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

  def avoidDetection(unit: FriendlyUnitInfo): Force = {
    if ( ! unit.cloaked) return new Force
    val detectors   = unit.matchups.enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
    val forces      = detectors.map(detector => towardsUnit(unit, detector, -1.0 / detector.pixelDistanceCenter(unit)))
    val output      = ForceMath.sum(forces).normalize
    output
  }

  /////////////////////////
  // Separation/Cohesion //
  /////////////////////////

  def regroup(unit: FriendlyUnitInfo): Force = {
    unit.squad.orElse(unit.team)
      .filter(_.attackers.size > 1)
      .map(g => ?(unit.flying, g.centroidKey, g.centroidGround))
      .map(towards(unit, _))
      .getOrElse(new Force)
  }

  private def collisionRepulsionMagnitude(unit: FriendlyUnitInfo, other: UnitInfo, margin: Double): Double = {
    if (unit.flying) return 0.0
    if (other.flying) return 0.0
    if (other.velocity.lengthSquared < 0.01) return 0.0
    if (other.topSpeed >= unit.topSpeed && other.speedApproaching(unit) < 0) return 0.0
    val output = 1.0 - Maff.clamp(other.pixelDistanceEdge(unit) / margin, 0.0, 1.0)
    output
  }
  def collisionRepulsion(unit: FriendlyUnitInfo, other: UnitInfo): Force = {
    val magnitude = collisionRepulsionMagnitude(unit, other, 12.0)
    if (magnitude == 0) new Force else towardsUnit(unit, other, -magnitude)
  }
  
  def avoidCollision(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    val repulsions = unit.inTileRadius(3).filterNot(unit==).map(collisionRepulsion(unit, _))
    ForceMath.mean(repulsions).clipAtMost(1.0)
  }

  def avoidSplash(unit: FriendlyUnitInfo): Force = {
    val splashThreats = unit.matchups.threats
      .filter(_.unitClass.dealsRadialSplashDamage)
      .filter(_.pixelsToGetInRange(unit) < 96)
      .filterNot(_.burrowed) // Ignore Spider Mines until they unburrow
      .toVector
    lazy val splashRadius25 = Maff.max(splashThreats.map(t => if (unit.flying) t.unitClass.airSplashRadius25 else t.unitClass.groundSplashRadius25)).getOrElse(0)
    lazy val splashRadius50 = Maff.max(splashThreats.map(t => if (unit.flying) t.unitClass.airSplashRadius50 else t.unitClass.groundSplashRadius50)).getOrElse(0)
    lazy val splashAllies   = unit.alliesBattle.filter(a => ! a.unitClass.isBuilding && a.flying == unit.flying)
    if (splashThreats.isEmpty)  return new Force
    if (splashRadius25 <= 0)    return new Force
    if (splashAllies.isEmpty)   return new Force
    val forces  = splashAllies.map(_.pixel).map(hardAvoid(unit, _, splashRadius50, splashRadius25))
    val output  = ForceMath.sum(forces).normalize(forces.map(_.lengthFast).max)
    output
  }

  def preferSpacing(unit: FriendlyUnitInfo): Force = {
    val splashForce = avoidSplash(unit)
    if (splashForce.lengthSquared > 0) splashForce else avoidCollision(unit)
  }

  def followPushes(unit: FriendlyUnitInfo): Force = {
    unit.agent.receivedPushForce().clipAtMost(1.0)
  }
}

