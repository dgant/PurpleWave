package Strategery.Strategies.Protoss.PvZ

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.PvE4GateAllIn
import Strategery.Strategies.Strategy
import bwapi.Race

object PvZ4GateDragoonAllIn extends Strategy {
  
  override def buildGameplan(): Option[Plan] = Some(new PvE4GateAllIn)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
