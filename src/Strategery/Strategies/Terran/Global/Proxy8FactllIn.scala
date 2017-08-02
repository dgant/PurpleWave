package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.Proxy8Fact
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy8FactllIn
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new Proxy8Fact) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
}
