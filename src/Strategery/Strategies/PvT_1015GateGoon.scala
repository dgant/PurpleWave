package Strategery.Strategies

import bwapi.Race

object PvT_1015GateGoon extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
