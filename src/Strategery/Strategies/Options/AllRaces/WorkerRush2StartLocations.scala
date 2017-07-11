package Strategery.Strategies.Options.AllRaces

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.ProbeRush
import Strategery.Strategies.Strategy

object WorkerRush2StartLocations extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new ProbeRush) }
  
  override def startLocationsMin = 2
  override def startLocationsMax = 2
}
