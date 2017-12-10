package Strategery.Strategies.Protoss.Other

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.ProtossHuntersFFAGatewayAggro
import Strategery.Maps.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossHuntersFFAAggroGateway extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new ProtossHuntersFFAGatewayAggro) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def requiredMaps: Iterable[StarCraftMap] = Vector(Hunters)
}
