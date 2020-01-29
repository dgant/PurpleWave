package Micro.Actions.Commands

import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.Points.PixelRay
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Gravitate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.forces.nonEmpty
  )
  
  private val rayDistance = 32.0 * 3.0
  private val cardinal8directions = (0.0 until 2.0 by 0.25).map(_ * Math.PI).toVector
  
  def useShortAreaPathfinding(unit: FriendlyUnitInfo): Boolean = {
    ! unit.flying && ! unit.transport.exists(_.flying) && unit.agent.destination.zone == unit.zone
  }

  
  override def perform(unit: FriendlyUnitInfo) {
    val framesAhead = With.reaction.agencyAverage + 1
    val minDistance = Math.max(48, unit.unitClass.haltPixels + framesAhead * (unit.topSpeed + 0.5 * framesAhead * unit.topSpeed / Math.max(1, unit.unitClass.accelerationFrames)))
    val forces      = unit.agent.forces.values
    val origin      = unit.pixelCenter
    val forceSum    = ForceMath.sum(forces).normalize
    val forceTotal  = ByOption.maxBy(unit.agent.resistances.values.flatten)(_.lengthSquared).foldLeft(forceSum)(ForceMath.resist(_, _).normalize)
    val rayLength   = Math.max(rayDistance, minDistance)
    val pathfind    = useShortAreaPathfinding(unit)
    def makeRay(radians: Double): PixelRay = {
      PixelRay(unit.pixelCenter, unit.pixelCenter.radiateRadians(radians, rayLength))
    }
    
    lazy val forceRadians = forceTotal.radians
    lazy val ray          = makeRay(forceRadians)
    lazy val rayWalkable  = ray.tilesIntersected.forall(With.grids.walkable.get)
    
    if ( ! pathfind || rayWalkable) {
      var destination = unit.pixelCenter.add(forceTotal.normalize(rayLength).toPoint)
      // Prevent unit from getting confused from trying to move too close to unwalkable tiles
      if ( ! unit.unitClass.corners.map(destination.add(_).tileIncluding).forall(unit.canTraverse)) {
        destination = destination.tileIncluding.pixelCenter
      }
      unit.agent.toTravel = Some(destination)
      
      return
    }
    
    val angles = cardinal8directions.filter(r => Math.abs(PurpleMath.radiansTo(r, forceRadians)) <= Math.PI * 0.75)
    val paths = angles.map(makeRay) :+ ray
    val pathsTruncated = paths.map(ray =>
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
    
    val pathAccepted = pathsTruncated.maxBy(ray => ray.length * (1.0 + Math.cos(ray.radians - forceRadians)))
    val commandPixel = unit.pixelCenter.project(pathAccepted.to, minDistance)
    unit.agent.pathsAll = paths
    unit.agent.pathsTruncated = pathsTruncated
    unit.agent.pathsAcceptable = Vector(pathAccepted)
    unit.agent.toTravel = Some(commandPixel)

    if (!unit.flying) {
      unit.zone.edges.find(e => unit.pixelDistanceCenter(e.pixelCenter) < e.radiusPixels).foreach(edge => {
        unit.agent.toTravel = Some(
          unit.pixelCenter.project(
            edge.sidePixels.minBy(ep => PurpleMath.radiansTo(
              unit.pixelCenter.radiansTo(commandPixel),
              edge.pixelCenter.radiansTo(ep))),
          128))
      })
    }
  }
}
