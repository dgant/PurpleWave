package Strategery.Strategies.Zerg.Global

import Planning.Plan
import Planning.Plans.Zerg.GamePlans.ProxyHatch
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyHatchZerglings extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def buildGameplan(): Option[Plan] = Some(new ProxyHatch)
}
