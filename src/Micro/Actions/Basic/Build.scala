package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.Fight
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBuild.isDefined &&
    unit.agent.toBuildTile.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val buildArea = unit.agent.toBuild.get.tileArea.add(unit.agent.toBuildTile.get)
    val blockers  = buildArea.expand(2, 2).tiles.flatMap(With.grids.units.get(_).filter(blocker =>
      blocker != unit   &&
      ! blocker.flying  &&
      blocker.possiblyStillThere))
    
    blockers.flatMap(_.friendly).foreach(_.agent.shove(unit))
    if (blockers.exists(_.isEnemy)) {
      unit.agent.canFight = true
      Fight.consider(unit)
    }
    else {
      With.commander.build(unit, unit.agent.toBuild.get, unit.agent.lastIntent.toBuildTile.get)
    }
  }
}
