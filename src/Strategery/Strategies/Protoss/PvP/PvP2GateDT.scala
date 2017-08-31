package Strategery.Strategies.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object PvP2GateDT extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
