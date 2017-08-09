package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.Proxy5Rax
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxy5RaxAllIn extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new Proxy5Rax) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def startLocationsMax = 2
}
