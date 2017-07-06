package Strategery.Strategies

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.IslandCarriers
import bwapi.Race

object IslandCarriers extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new IslandCarriers)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
