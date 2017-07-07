package Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro.Early

import Strategery.Strategies.Strategy
import bwapi.Race

object EarlyDTExpand extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
