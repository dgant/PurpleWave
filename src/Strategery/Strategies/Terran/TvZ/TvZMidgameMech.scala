package Strategery.Strategies.Terran.TvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object TvZMidgameMech extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}
