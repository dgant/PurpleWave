package Micro.Actions.Combat.Maneuvering

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Traverse(path: TilePath) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && path.pathExists

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.path = Some(path)
    path.tiles.get.foreach(With.coordinator.gridPathOccupancy.addUnit(unit, _))
    val lookaheadDefault = (2 * unit.unitClass.haltPixels + unit.topSpeed * With.reaction.agencyAverage / 32).toInt
    val lookaheadFinal = PurpleMath.clamp(lookaheadDefault, 8, 12)
    unit.agent.toTravel = Some(path.tiles.get.take(lookaheadFinal).last.pixelCenter)
    Move.delegate(unit)
  }
}
