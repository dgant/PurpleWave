package Strategery.Strategies.Protoss

import Planning.Plan
import Planning.Plans.Gameplans.Protoss.PvR.PvR2Gate4Gate
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class PvRStrategy extends Strategy {
  override def ourRaces    : Seq[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Seq[Race] = Vector(Race.Unknown)
}

object PvR2Gate4Gate extends PvRStrategy {
  override def gameplan: Option[Plan] = Some(new PvR2Gate4Gate)
}

