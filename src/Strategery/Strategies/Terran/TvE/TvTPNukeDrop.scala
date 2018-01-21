package Strategery.Strategies.Terran.TvE

import Strategery.Strategies.Strategy
import bwapi.Race

object TvTPNukeDrop extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Terran, Race.Protoss)
}
