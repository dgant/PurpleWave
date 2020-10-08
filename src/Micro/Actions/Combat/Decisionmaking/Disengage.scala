package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Weigh
import Micro.Actions.Combat.Techniques._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Disengage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canFlee
    && unit.matchups.threats.nonEmpty
    && ! With.yolo.active()
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    EngageDisengage.NewDisengage.consider(unit)
  }

  private def weigh(unit: FriendlyUnitInfo): Unit = {
    Weigh.consider(unit,
      Abuse,
      Aim,
      Bomb,
      Avoid,
      FallBack,
      Ignore,
      Scratch,
      Purr)
  }
}
