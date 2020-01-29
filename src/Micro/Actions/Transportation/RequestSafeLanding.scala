package Micro.Actions.Transportation

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class RequestSafeLanding(destination: Option[Pixel] = None) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.exists(_.canMove)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val goal = destination.getOrElse(if (unit.agent.shouldEngage) unit.agent.destination else unit.agent.origin)
    val distanceToGoal = unit.pixelDistanceCenter(goal)
    val searchOriginTile = unit.pixelCenter.project(goal, Math.min(distanceToGoal, 96)).tileIncluding
    val landingOption = Spiral
      .points(6)
      .map(searchOriginTile.add)
      .find(tile =>
        tile.valid && With.grids.walkable.get(tile) && unit.enemyRangeGrid.get(tile) <= 0
      )
    landingOption.foreach(landing => {
      unit.agent.directRide(landing.pixelCenter)
      if (unit.pixelDistanceEdge(landing.pixelCenter) < 48) {
        With.commander.unload(unit.transport.get, unit)
      }
    })

  }
}
