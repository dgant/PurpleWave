package Strategery.Strategies.Protoss.FFA

import Planning.Plan
import Planning.Plans.GamePlans.Protoss.Standard.FFA.ProtossHuntersFFA
import Strategery.{Hunters, StarCraftMap}
import Strategery.Strategies.Strategy
import bwapi.Race

object ProtossHuntersFFA extends Strategy {

  override def gameplan: Option[Plan] = { Some(new ProtossHuntersFFA) }
  
  override def ourRaces: Iterable[Race] = Vector(Race.Protoss)
  override def ffa = true
  
  override def mapsWhitelisted: Option[Iterable[StarCraftMap]] = Some(Vector(Hunters))
}
