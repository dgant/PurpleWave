package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.MassMarine
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEMassMarine extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassMarine) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
