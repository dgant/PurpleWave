package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.MassBio
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEMassBio extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassBio) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
