package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Targeting.Target
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Charge extends ActionTechnique {
  
  // Just run up and kill something.
  // eg. Zealot vs. Siege Tank
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && unit.canMove
    && unit.matchups.targets.nonEmpty
  )
  
  override val applicabilityBase = 0.5
  
  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) return None
    if (unit.canAttack(other)) return Some(1.0)
    if (other.canAttack(unit)) return Some(0.0)
    Some(1.0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Target.delegate(unit)
    Attack.delegate(unit)
  }
}
