package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Techniques.Common.{ActionTechnique, ShootAsSoonAsPossible}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Reposition extends ActionTechnique {
  
  // Find a better place to stand while on cooldown.
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.canAttack
  )
  
  override val activator = One
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    - unit.matchups.vpfNetDiffused
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    ShootAsSoonAsPossible.delegate(unit)
    Avoid.delegate(unit)
  }
}
