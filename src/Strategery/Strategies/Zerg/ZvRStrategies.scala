package Strategery.Strategies.Zerg

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.ZvR9Pool
import bwapi.Race

abstract class ZvRStrategy extends ZergStrategy {
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown)
}

object ZvR9Pool extends ZvRStrategy {
  override def gameplan: Option[Plan] = Some(new ZvR9Pool)
}