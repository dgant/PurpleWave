package Strategery.Strategies.Terran.TvZ

import Planning.Plan
import Planning.Plans.Terran.GamePlans.TerranStandardGamePlan
import Strategery.Strategies.Strategy
import bwapi.Race

object TvZStandard extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new TerranStandardGamePlan)
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      TvZEarlyCCFirst,
      TvZEarly1RaxFEEconomic,
      TvZEarly1RaxFEConservative,
      TvZEarly2Rax))
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
