package Strategery.Strategies

import Planning.Plan
import Planning.Plans.Protoss._
import bwapi.Race

object IslandCarriers extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new GamePlans.IslandCarriers)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
