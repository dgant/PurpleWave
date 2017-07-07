package Strategery.Strategies.Options.Protoss.Choices

import Planning.Plan
import Planning.Plans.Protoss._
import Strategery.Strategies.Strategy
import bwapi.Race

object IslandCarriers extends Strategy {
  
  override lazy val gameplan: Option[Plan] = Some(new GamePlans.IslandCarriers)
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def islandMaps: Boolean = true
  override def groundMaps: Boolean = false
}
