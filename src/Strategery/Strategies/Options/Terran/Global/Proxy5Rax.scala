package Strategery.Strategies.Options.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.Proxy5Rax
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy5Rax
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new Proxy5Rax) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def startLocationsMin = 2
  override def startLocationsMax = 2
}
