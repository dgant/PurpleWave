package Strategery.Strategies.Options.Protoss.PvTMacro

import Strategery.Strategies.Strategy
import bwapi.Race

object Early1015GateGoon extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}
