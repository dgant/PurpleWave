package Micro.Actions.Commands

import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.Points.PixelRay
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gravitate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.forces.nonEmpty
  )
  
  private val rayDistance = 32.0 * 4.0
  private val cardinal8directions = (0.0 until 2.0 by 0.25).map(_ * Math.PI).toVector
  
  override def perform(unit: FriendlyUnitInfo) {
    val framesAhead     = With.reaction.agencyAverage + 1
    val distance        = unit.unitClass.haltPixels + framesAhead * (unit.topSpeed + framesAhead * unit.unitClass.accelerationFrames / 2)
    val forces          = unit.agent.forces.values
    val origin          = unit.pixelCenter
    val forceTotal      = ForceMath.sum(forces)
    
    
    if (unit.flying) {
      val destination = unit.pixelCenter.add(forceTotal.normalize(distance).toPoint)
      unit.agent.toTravel = Some(destination)
      return
    }
    
    val forceRadians    = forceTotal.radians
    val angles          = cardinal8directions.filter(r => Math.abs(r - forceRadians) <= Math.PI / 2.0) :+ forceRadians
    val paths           = angles.map(angle => PixelRay(unit.pixelCenter, unit.pixelCenter.radiateRadians(angle, rayDistance)))
    val pathsTruncated  = paths.map(ray =>
      PixelRay(
        ray.from,
        ray.from.project(
          ray.to,
          ray
            .tilesIntersected
            .takeWhile(tile => tile.valid && With.grids.walkable.get(tile))
            .lastOption
            .map(_.pixelCenter.pixelDistance(ray.from))
            .getOrElse(0.0))))
    
    val pathAccepted = pathsTruncated.maxBy(ray => ray.length * Math.cos(ray.radians - forceRadians))
    
    unit.agent.pathsAll = paths
    unit.agent.pathsTruncated = pathsTruncated
    unit.agent.pathsAcceptable = Vector(pathAccepted)
    unit.agent.toTravel = Some(pathAccepted.to)
  }
}
