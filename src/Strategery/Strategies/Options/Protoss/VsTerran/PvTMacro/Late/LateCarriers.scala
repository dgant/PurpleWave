package Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro.Late

import Strategery.Strategies.Strategy
import bwapi.Race

object LateCarriers extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}