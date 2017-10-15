package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.Proxy8Fact
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy8FactAllIn extends Strategy {
  
  // Currently unusable due to delayed gas mining.
  
  override def gameplan: Option[Plan] = { Some(new Proxy8Fact) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def startLocationsMax = 2
}
