package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvETurtleMech
import Strategery.Strategies.Strategy
import bwapi.Race

object TvETurtleMech extends Strategy {
  
  override def gameplan: Option[Plan] = Some(new TvETurtleMech)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
