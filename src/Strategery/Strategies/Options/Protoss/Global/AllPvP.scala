package Strategery.Strategies.Options.Protoss.Global

import Strategery.Strategies.Strategy
import bwapi.Race

object AllPvP extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
