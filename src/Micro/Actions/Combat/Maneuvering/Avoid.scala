package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.Points.PixelRay
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Avoid extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove &&
    unit.matchups.threats.nonEmpty
  }
  
  private val cardinal8Angles = (0.0 until 2.0 by 0.25).map(_ * Math.PI).toVector
  
  override def perform(unit: FriendlyUnitInfo) {
    
    /*
    There are multiple different ways to avoid someone:
    
    1. Move directly away (which is great until you hit a wall)
    2. Move to the exit closest to home (best in the long term, but you may die along the way)
    3. Move to the nearest exit (good in the medium term; bad when you're initially getting hit and bad long-term if you're outsped)
    4. Move to help (make chasers run into a bunch of Siege Tanks, for example)
    
    So we want to pick the appropriate getaway technique based on the situation.
    */
  
    
    
    val threats             = unit.matchups.mostEntangledThreatsDiffused.take(8)
    val zoneUs              = unit.pixelCenter.zone
    val exits               = zoneUs.edges
    val idealExtraDistance  = threats.map(idealExtraDistancePixels(unit, _)).max
  
    val trapped     = ! unit.flying && (zoneUs.owner.isUs || exits.forall(exit => unit.pixelDistanceFast(exit.centerPixel) > threats.map(_.pixelDistanceFast(exit.centerPixel)).min))
    val mustEscape  = ! trapped && idealExtraDistance.isInfinity
    
    if (mustEscape) {
      Retreat.consider(unit)
    }
    
    val anglesAway        = threats.map(threat => PurpleMath.normalizeAngle(threat.pixelCenter.radiansTo(unit.pixelCenter)))
    val angles            = anglesAway ++ cardinal8Angles
    val paths             = angles.map(angle => PixelRay(unit.pixelCenter, unit.pixelCenter.radiateRadians(angle, idealExtraDistance * 1.5)))
    val pathsTruncated    = paths.map(ray => PixelRay(ray.from, ray.from.project(ray.to, ray
      .tilesIntersected
      .takeWhile(tile => tile.valid && (unit.flying || With.grids.walkable.get(tile)))
      .lastOption
      .map(_.pixelCenter.pixelDistanceFast(ray.from))
      .getOrElse(0.0))))
    val pathsAcceptable   = pathsTruncated.filter(_.lengthFast >= idealExtraDistance)
    val pathAccepted      = ByOption.minBy(pathsAcceptable)(path => unit.matchups.ifAt(path.from.project(path.to, 8.0)).framesOfEntanglementDiffused)
    
    unit.agent.pathsAll         = paths
    unit.agent.pathsTruncated   = pathsTruncated
    unit.agent.pathsAcceptable  = pathsAcceptable
    unit.agent.pathAccepted     = pathAccepted
    
    if (pathAccepted.isDefined) {
      // If we want to move at non-orthogonal angles we need to restrict moves to orders of < 64 pixels.
      
      val acceptedPathAngle = pathAccepted.get.from.radiansTo(pathAccepted.get.to)
      val offsetFrom8       = cardinal8Angles.map(angle => Math.abs(angle - acceptedPathAngle)).min
      val moveTarget        = if (offsetFrom8 < Math.PI / 32.0) pathAccepted.get.to else unit.pixelCenter.project(pathAccepted.get.to, 60.0)
      With.commander.move(unit, moveTarget)
    }
    else {
      Retreat.delegate(unit)
    }
  }
  
  def idealExtraDistancePixels(unit: FriendlyUnitInfo, threat: UnitInfo): Double = {
    val rangePixelsEdgeThem = threat.pixelRangeAgainstFromEdge(unit)
    val rangePixelsEdgeUs   = if (unit.canAttack(threat)) unit.pixelRangeAgainstFromEdge(threat) else 0.0
    
    val rangeAdvantage =  rangePixelsEdgeUs - rangePixelsEdgeThem
    if (rangeAdvantage > 0) return rangeAdvantage
    
    if (threat.topSpeedChasing > unit.topSpeed) return Double.PositiveInfinity
    
    rangePixelsEdgeThem + 48.0 - unit.pixelDistanceFast(threat)
  }
}
