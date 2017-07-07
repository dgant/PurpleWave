package Strategery.Strategies.Options.PvT_Macro

import Strategery.Strategies.Strategy
import bwapi.Race

object LateMassGateway extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Random, Race.Terran)
}