package Strategery.Strategies.Terran.TvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object TvZMidgameMech extends Strategy {
  
  override def ourRaces: Seq[Race] = Seq(Race.Terran)
  override def enemyRaces: Seq[Race] = Seq(Race.Zerg)
}
