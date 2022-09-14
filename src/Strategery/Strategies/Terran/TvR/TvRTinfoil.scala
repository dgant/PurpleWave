package Strategery.Strategies.Terran.TvR

import Strategery.Strategies.Strategy
import bwapi.Race

object TvRTinfoil extends Strategy {
  
  override def ourRaces    : Seq[Race] = Seq(Race.Terran)
  override def enemyRaces  : Seq[Race] = Seq(Race.Unknown)
}
