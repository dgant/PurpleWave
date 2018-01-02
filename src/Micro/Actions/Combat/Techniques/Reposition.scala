package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Maneuvering.OldAttackAndReposition
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Reposition extends ActionTechnique {
  
  // Find a better place to stand while on cooldown.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
    && unit.unitClass.ranged
  )
  
  override val activator = One
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    - unit.matchups.vpfNetDiffused
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.readyForAttackOrder
      || unit.matchups.targetsInRange.isEmpty
      || unit.matchups.targets.forall(t => unit.pixelsFromEdgeFast(t) > unit.pixelRangeAgainstFromEdge(t) - 32.0)) {
      Attack.delegate(unit)
    }
    OldAttackAndReposition.delegate(unit)
  }
}
