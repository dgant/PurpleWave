package Strategery.Strategies.Protoss.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.FFA.ProtossFFA
import Strategery.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossFFA extends Strategy {

  override def gameplan: Option[Plan] = { Some(new ProtossFFA) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def mapsBlacklisted: Iterable[StarCraftMap] = Vector(Hunters)
}
