package Strategery.Strategies.Protoss.Global

import Planning.Plan
import Planning.Plans.Protoss._
import Strategery.Strategies.Strategy
import bwapi.Race

object IslandCarriers extends Strategy {
  
  override def buildGameplan(): Option[Plan] = {  Some(new GamePlans.IslandCarriers) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
