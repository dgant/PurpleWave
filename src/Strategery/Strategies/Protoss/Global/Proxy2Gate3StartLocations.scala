package Strategery.Strategies.Protoss.Global

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Proxy2GateInMiddle
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy2Gate3StartLocations extends Strategy {
  
  override def buildGameplan(): Option[Plan] = { Some(new Proxy2GateInMiddle) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def startLocationsMin = 3
}
