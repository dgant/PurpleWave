package Strategery.Strategies.Options.Protoss.Choices

import Strategery.Strategies.Strategy
import bwapi.Race

object PvP_Macro extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Protoss)
}
