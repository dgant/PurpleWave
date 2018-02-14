package Strategery.Strategies.Terran.TvE

import Planning.Plan
import Planning.Plans.GamePlans.Terran.Standard.TvE.MassGoliath
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEMassGoliath extends Strategy {
  
  override def gameplan: Option[Plan] = { Some(new MassGoliath) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
