package Strategery.Strategies.Terran.TvP

import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvE.TerranStandardGamePlan
import Strategery.Strategies.Strategy
import bwapi.Race

object TvPStandard extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TerranStandardGamePlan) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}
