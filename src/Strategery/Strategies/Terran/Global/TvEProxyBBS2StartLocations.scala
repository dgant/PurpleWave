package Strategery.Strategies.Terran.Global

import Planning.Plan
import Planning.Plans.Terran.GamePlans.ProxyBBS
import Strategery.Strategies.Strategy
import bwapi.Race

object TvEProxyBBS2StartLocations extends Strategy {
  
  override def gameplan(): Option[Plan] = { Some(new ProxyBBS) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)

  override def startLocationsMax = 2
}
