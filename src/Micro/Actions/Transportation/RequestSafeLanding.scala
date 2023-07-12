package Micro.Actions.Transportation

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
import Micro.Agency.Commander
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object RequestSafeLanding {

  def apply(unit: FriendlyUnitInfo, destination: Option[Pixel] = None): Unit = {
    if (unit.airlifted) {
      val goal              = destination.getOrElse(?(unit.agent.shouldFight, unit.agent.destination, unit.agent.safety))
      val distanceToGoal    = unit.pixelDistanceCenter(goal)
      val searchOriginTile  = unit.pixel.project(goal, Math.min(distanceToGoal, 96)).tile
      val landingOption     = Spiral(6)
        .map(searchOriginTile.add)
        .find(tile => tile.walkable && tile.enemyRange < With.grids.enemyRangeGround.margin)

      landingOption.foreach(landing => {
        unit.agent.setRideGoal(landing.center)
        if (unit.pixelDistanceEdge(landing.center) < 48) {
          Commander.unload(unit.transport.get, unit)
        }
      })
    }
  }
}
