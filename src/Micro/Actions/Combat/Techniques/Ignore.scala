package Micro.Actions.Combat.Techniques

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Micro.Actions.Combat.Decisionmaking.Engage
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Combat.Techniques.Common.Activators.One
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Ignore extends ActionTechnique {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
  )
  
  override val activator = One
  
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = {
    unit.matchups.framesOfSafetyDiffused / GameTime(0, 2)() - 1.0
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.destination.zone == unit.zone) {
      Engage.delegate(unit)
    }
  }
}
