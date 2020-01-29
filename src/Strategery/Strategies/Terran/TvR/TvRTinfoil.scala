package Strategery.Strategies.Terran.TvR

import Strategery.Strategies.Strategy
import bwapi.Race

object TvRTinfoil extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
