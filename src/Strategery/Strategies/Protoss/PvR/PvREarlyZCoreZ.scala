package Strategery.Strategies.Protoss.PvR

import Strategery.Strategies.Strategy
import bwapi.Race

object PvREarlyZCoreZ extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
