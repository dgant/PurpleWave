package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Weigh
import Micro.Actions.Combat.Techniques._
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Disengage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canFlee
    && unit.matchups.threats.nonEmpty
    && ! Yolo.active
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    Weigh.consider(unit,
      Abuse,
      Aim,
      Avoid,
      FallBack,
      Ignore,
      Isolate,
      Purr,
      Retreat)
  }
}
