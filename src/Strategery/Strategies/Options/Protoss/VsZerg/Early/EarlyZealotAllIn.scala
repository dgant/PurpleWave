package Strategery.Strategies.Options.Protoss.VsZerg.Early

import Strategery.Strategies.Strategy
import bwapi.Race

object EarlyZealotAllIn extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Zerg)
}
