package Strategery.Strategies

import bwapi.Race

object PvZ_FFE extends Strategy {
  
  override def ourRaces       : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Zerg)
}
