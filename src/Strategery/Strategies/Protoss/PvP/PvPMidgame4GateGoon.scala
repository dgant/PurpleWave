package Strategery.Strategies.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object PvPMidgame4GateGoon extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Protoss)
}
