package Strategery.Strategies.Options.Protoss.PvT

import Strategery.Strategies.Strategy
import bwapi.Race

object PvTLateMassGateway extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}