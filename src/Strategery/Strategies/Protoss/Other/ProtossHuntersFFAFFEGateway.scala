package Strategery.Strategies.Protoss.Other

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ProtossHuntersFFAFFEGateway
import Strategery.Maps.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossHuntersFFAFFEGateway extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new ProtossHuntersFFAFFEGateway) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def requiredMaps: Iterable[StarCraftMap] = Vector(Hunters)
}
