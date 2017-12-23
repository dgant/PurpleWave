package Strategery.Strategies.Protoss.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.FFA.ProtossBigFFACarriers
import Strategery.Maps.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossBigFFACarriers extends Strategy {
  
  override def gameplan: Option[Plan] = {  Some(new ProtossBigFFACarriers) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def prohibitedMaps: Iterable[StarCraftMap] = Vector(Hunters)
}
