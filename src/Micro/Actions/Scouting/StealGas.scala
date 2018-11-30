package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Basic.Build
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object StealGas extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = With.blackboard.stealGas()

  override def perform(unit: FriendlyUnitInfo): Unit = {

    unit.agent.toBuild = Some(With.self.raceCurrent match {
      case Race.Terran => Terran.Refinery;
      case Race.Protoss => Protoss.Assimilator;
      case Race.Zerg => Zerg.Extractor
      case _ => Terran.Refinery
    })
    unit.agent.toBuildTile = unit.base.flatMap(_.gas.headOption.map(_.tileTopLeft))
    Build.delegate(unit)
  }
}
