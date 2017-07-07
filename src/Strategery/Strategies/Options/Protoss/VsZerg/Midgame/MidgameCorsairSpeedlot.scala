package Strategery.Strategies.Options.Protoss.VsZerg.Midgame

import Strategery.Strategies.Strategy
import bwapi.Race

object MidgameCorsairSpeedlot extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Zerg)
}
