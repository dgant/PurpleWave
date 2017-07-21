package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Fight
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.toBuild.isDefined &&
    unit.action.toBuildTile.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val buildArea = unit.action.toBuild.get.tileArea.add(unit.action.toBuildTile.get)
    val blockers  = unit.matchups.others.filter(_.tileArea.intersects(buildArea))
    blockers.flatMap(_.friendly).foreach(_.action.shove(unit))
    if (blockers.exists(_.isEnemy)) {
      Fight.consider(unit)
    }
    else {
      With.commander.build(unit, unit.action.toBuild.get, unit.action.intent.toBuildTile.get)
    }
  }
}
