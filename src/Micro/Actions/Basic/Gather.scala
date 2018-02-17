package Micro.Actions.Basic

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  private val combatWindow = GameTime(0, 2)()
  
  override def perform(unit: FriendlyUnitInfo) {
  
    Potshot.consider(unit)
    lazy val resource     = unit.agent.toGather.get
    lazy val zoneNow      = unit.zone
    lazy val zoneTo       = resource.zone
    lazy val transferring = zoneNow != zoneTo && ! zoneNow.edges.exists(_.otherSideof(zoneNow) == zoneTo)
    lazy val threatened   = unit.matchups.framesOfSafetyDiffused < combatWindow
    lazy val atResource   = unit.pixelDistanceEdge(resource) < 32.0 * 5.0
    lazy val beckoned     = unit.matchups.targets.exists(target =>
        ! target.unitClass.isWorker
        && With.framesSince(target.lastAttackStartFrame) < combatWindow
        && unit.framesToGetInRange(target) < combatWindow
        && target.matchups.targets.exists(ally =>
          ally.totalHealth < 200
          && ally.canAttack
          && ally.matchups.framesOfEntanglementPerThreatDiffused
            .get(target)
            .exists(_ < GameTime(0, 2)())))
    
    if (atResource && unit.totalHealth > 13 && beckoned) {
      Engage.consider(unit)
    }
    if (transferring && threatened) {
      unit.agent.canFight = false
      Disengage.consider(unit)
    }
    
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
