package Strategery.Strategies.Protoss.Other

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ThreeBaseCarriersWithNoDefense
import Strategery.Strategies.Strategy
import bwapi.Race

object CarriersFromAnIsland extends Strategy {
  
  override def buildGameplan(): Option[Plan] = {  Some(new ThreeBaseCarriersWithNoDefense) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
