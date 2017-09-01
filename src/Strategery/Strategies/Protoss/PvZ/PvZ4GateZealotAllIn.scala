package Strategery.Strategies.Protoss.PvZ

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.PvZ4GateAllIn
import Strategery.Strategies.Strategy
import bwapi.Race

object PvZ4GateZealotAllIn extends Strategy {
  
  override def buildGameplan(): Option[Plan] = Some(new PvZ4GateAllIn)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
