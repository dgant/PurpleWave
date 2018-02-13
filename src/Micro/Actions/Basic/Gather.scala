package Micro.Actions.Basic

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Tactics.Potshot
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    Potshot.consider(unit)
    lazy val resource     = unit.agent.toGather.get
    lazy val zoneNow      = unit.zone
    lazy val zoneTo       = resource.zone
    lazy val transferring = With.geography.ourZones.forall(z => z != zoneNow && z != zoneTo)
    lazy val threatened   = unit.matchups.framesOfSafetyDiffused < GameTime(0, 2)()
    lazy val beckoned =
      unit.matchups.targets.exists(target =>
        ! target.unitClass.isWorker
        && target.target.exists(ally =>
          ally.pixelDistanceEdge(unit) < 128))
    /*
    val resource = unit.agent.toGather.get
    if (unit.agent.toGather.exists(_.zone == unit.zone)) {
      if (unit.matchups.threats.exists(_.framesBeforeAttacking(unit) < 36)) {
        Fight.consider(unit)
      }
    }
    else {
      if (unit.matchups.framesOfSafetyDiffused < 24)
    }
    */
    
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
