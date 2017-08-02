package Strategery.Strategies.Options.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.BBSInMiddle
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyBBS3StartLocations
  extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new BBSInMiddle) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  
  override def startLocationsMin = 3
}
