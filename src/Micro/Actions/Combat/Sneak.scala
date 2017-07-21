package Micro.Actions.Combat

import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.cloaked &&
    unit.matchups.threats.nonEmpty &&
    unit.matchups.enemies.exists(e => e.unitClass.isDetector && e.aliveAndComplete)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)
    Retreat.delegate(unit)
  }
}
