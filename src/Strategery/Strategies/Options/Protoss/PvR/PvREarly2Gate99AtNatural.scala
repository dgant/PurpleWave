package Strategery.Strategies.Options.Protoss.PvR

import Strategery.Strategies.Strategy
import bwapi.Race

object PvREarly2Gate99AtNatural extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
