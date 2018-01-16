package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.TvE.MassVulture
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEMassVulture extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassVulture) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
