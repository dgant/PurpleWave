package Strategery.Strategies

import bwapi.Race

object PvZ_2Gate extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
