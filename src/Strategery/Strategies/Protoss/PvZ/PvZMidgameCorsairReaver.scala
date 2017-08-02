package Strategery.Strategies.Protoss.PvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object PvZMidgameCorsairReaver extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Zerg)
}
