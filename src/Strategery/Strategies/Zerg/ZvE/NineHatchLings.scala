package Strategery.Strategies.Zerg.ZvE

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.NineHatchLings
import Strategery.Strategies.Strategy
import bwapi.Race

object NineHatchLings extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = { Some(new NineHatchLings) }
}
