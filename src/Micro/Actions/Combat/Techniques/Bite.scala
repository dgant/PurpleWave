package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, Leave}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Bite extends ActionTechnique {
  
  // If cornered and unable to escape, fight back.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
    && unit.seeminglyStuck
    && unit.readyForAttackOrder
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Potshot.delegate(unit)
    Leave.delegate(unit)
  }
}
