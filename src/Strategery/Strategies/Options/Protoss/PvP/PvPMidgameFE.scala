package Strategery.Strategies.Options.Protoss.PvP

import Strategery.Strategies.Strategy
import bwapi.Race

object PvPMidgameFE extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown, Race.Protoss)
}
