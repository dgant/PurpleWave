package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Avoid
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Sneak extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.cloaked
    && unit.canMove
    && unit.agent.canFlee
    && ! With.yolo.active()
    && ! unit.agent.shouldEngage
    && unit.matchups.nearestArbiter.isEmpty
    && unit.matchups.enemyDetectors.isEmpty
    && unit.matchups.enemies.exists(e => e.complete && ! e.unitClass.isWorker && (if (unit.flying) e.unitClass.attacksGround else e.unitClass.attacksAir))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Potshot.delegate(unit)

    if ( ! unit.ready) return

    if ( ! unit.effectivelyCloaked || unit.tileArea.expand(2, 2).tiles.exists(With.grids.enemyDetection.isDetected)) {
      Avoid.delegate(unit)
    }
  }
}
