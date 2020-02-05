package Micro.Actions.Combat.Maneuvering

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Traverse(path: TilePath, move: Boolean = true) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && path.pathExists

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.path = Some(path)
    path.tiles.get.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))

    // 5 was the recommendation from McRave.
    // It avoids putting units in situations where they're trying to move just to the other side of a building,
    // which can cause them to get stuck.
    unit.agent.toTravel = Some(path.tiles.get.take(5).last.pixelCenter)

    if (move) {
      Move.delegate(unit)
    }
  }
}
