package Strategery.Strategies.Protoss.PvE

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.Proxy2Gate
import Strategery.Maps.MapGroups
import Strategery.Strategies.Strategy
import bwapi.Race

object Proxy6Gate extends Strategy {
  
  override def buildGameplan: Option[Plan] = { Some(new Proxy2Gate) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  
  override def startLocationsMax = 2
  
  override def prohibitedMaps = MapGroups.badForProxying
}
