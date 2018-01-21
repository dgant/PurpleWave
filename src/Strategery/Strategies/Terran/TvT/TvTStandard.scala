package Strategery.Strategies.Terran.TvT

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TerranStandardGamePlan
import Strategery.Strategies.Strategy
import bwapi.Race

object TvTStandard extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TerranStandardGamePlan) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran)
}
