package Micro.Actions.Combat.Tactics

import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters.{TargetFilter, TargetFilterCombatants, TargetFilterVisibleInRange}
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Commands.{Attack, Patrol}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Potshot extends Action {
  
  object PotshotTargetFilter extends TargetFilter {
    override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
      TargetFilterVisibleInRange.legal(actor, target)
      && TargetFilterCombatants.legal(actor, target)
      && ( ! target.unitClass.isBuilding || target.canAttack || target.unitClass.isDetector)
    )
  }
  
  object PotshotTarget extends TargetAction(PotshotTargetFilter)
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight       &&
    unit.readyForAttackOrder  &&
    unit.matchups.targetsInRange.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    PotshotTarget.delegate(unit)
    unit.target.foreach(target =>
      if (unit.unitClass.isArbiter && PurpleMath.radiansTo(unit.angleRadians, unit.pixelCenter.radiansTo(target.pixelCenter)) > Math.PI / 2) {
        unit.agent.toTravel = Some(target.pixelCenter)
        Patrol.delegate(unit)
      })
    Attack.delegate(unit)
  }
}
