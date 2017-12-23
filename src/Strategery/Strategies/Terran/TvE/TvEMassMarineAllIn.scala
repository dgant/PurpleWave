package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvE.MassMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEMassMarineAllIn extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassMarine) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
