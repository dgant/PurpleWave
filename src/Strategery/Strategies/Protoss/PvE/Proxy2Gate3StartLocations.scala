package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Proxy2Gate
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy2Gate3StartLocations extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new Proxy2Gate) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def startLocationsMin = 3
}
