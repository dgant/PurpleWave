package Strategery.Strategies.Terran.TvP

import Planning.Plan
import Planning.Plans.Terran.GamePlans.TerranStandardGamePlan
import Strategery.Strategies.Strategy
import bwapi.Race

object TvPStandard extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new TerranStandardGamePlan) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
