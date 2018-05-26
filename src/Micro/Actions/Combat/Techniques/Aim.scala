package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Targeting.Filters.TargetFilterVisibleInRange
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Aim extends ActionTechnique {
  
  // Choose a target and shoot.
  // eg. Sunken Colony
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && ! unit.canMove
  )
  
  object StaticTarget extends TargetAction(TargetFilterVisibleInRange)
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    StaticTarget.delegate(unit)
    Attack.delegate(unit)
  }
}
