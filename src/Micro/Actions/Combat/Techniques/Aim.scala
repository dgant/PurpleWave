package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Attacking.TargetInRange
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
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    TargetInRange.delegate(unit)
    Attack.delegate(unit)
  }
}
