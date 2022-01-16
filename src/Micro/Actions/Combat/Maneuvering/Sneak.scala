package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.cloaked
    && unit.canMove
    && unit.intent.canFlee
    && ! With.yolo.active
    && ( ! unit.agent.shouldEngage || unit.presumptiveTarget.exists(_.base.exists(_.owner.isUs)))
    && ! unit.alliesBattle.exists(Protoss.Arbiter)
    && unit.matchups.enemies.exists(e => e.complete && ! e.unitClass.isWorker && e.attacksAgainst(unit) > 0))
  
  override protected def perform(unit: FriendlyUnitInfo) {
    if ( ! unit.effectivelyCloaked || unit.tileArea.expand(2, 2).tiles.exists(With.grids.enemyDetection.inRange)) {
      Potshot.delegate(unit)
      Retreat.delegate(unit)
    }
  }
}
