package Strategery.Strategies.Options.Protoss.VsTerran.PvTMacro.Late

import Strategery.Strategies.Strategy
import bwapi.Race

object LateMassGateway extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}