package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvE2RaxMarineSCVAllIn
import Strategery.Strategies.Strategy
import bwapi.Race

object TvESCVMarineAllIn extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new TvE2RaxMarineSCVAllIn) }
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
