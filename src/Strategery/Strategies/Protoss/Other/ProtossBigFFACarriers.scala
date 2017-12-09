package Strategery.Strategies.Protoss.Other

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ProtossBigFFACarriers
import Strategery.Maps.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossBigFFACarriers extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new ProtossBigFFACarriers) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def prohibitedMaps: Iterable[StarCraftMap] = Vector(Hunters)
}
