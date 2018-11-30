package Micro.Actions.Combat.Tactics

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Weigh
import Micro.Actions.Combat.Techniques.{Sally, Hunker}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Phalanx extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toForm.isDefined

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    Weigh.consider(unit, Sally, Hunker)
  }
}
