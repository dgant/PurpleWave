package Strategery.Strategies

import bwapi.Race

object PvP_2GateReaverExpand extends Strategy {
  
  override def ourRaces       : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
