package Micro.Actions.Combat.Decisionmaking

import Micro.Actions.Action
import Micro.Actions.Combat.Techniques.Common.Weigh
import Micro.Actions.Combat.Techniques._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Engage extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight &&
    unit.matchups.targets.nonEmpty
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    EngageDisengage.NewEngage.consider(unit)
    //weigh(unit)
  }
  
  private def weigh(unit: FriendlyUnitInfo) {
    Weigh.consider(unit,
      Abuse,
      Aim,
      Batter,
      Bomb,
      Brawl,
      Breathe,
      Charge,
      Chase,
      Purr,
      Reposition,
      Spread
    )
  }
}
