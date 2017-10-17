package Strategery.Strategies.Protoss.PvZ

import Planning.Plan
import Planning.Plans.Protoss.GamePlans.Specialty.PvZFourGateAllIn
import Strategery.Strategies.Strategy
import bwapi.Race

object PvZ4GateDragoonAllIn extends Strategy {
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
