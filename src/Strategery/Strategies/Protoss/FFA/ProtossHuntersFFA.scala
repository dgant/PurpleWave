package Strategery.Strategies.Protoss.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.FFA.ProtossHuntersFFA
import Strategery.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossHuntersFFA extends Strategy {

  override def gameplan: Option[Plan] = { Some(new ProtossHuntersFFA) }
  
  override def ourRaces: Seq[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def mapsWhitelisted: Seq[StarCraftMap] = Vector(Hunters)
}
