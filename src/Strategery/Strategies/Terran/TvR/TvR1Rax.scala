package Strategery.Strategies.Terran.TvR

import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.{TerranChoices, TvT14CC}
import bwapi.Race

object TvR1Rax extends Strategy {
  
  override lazy val choices = Seq(
    TerranChoices.tvtOpeners.filterNot(TvT14CC==),
    TerranChoices.tvpOpeners,
    TerranChoices.tvzOpeners)
  
  override def ourRaces    : Seq[Race] = Seq(Race.Terran)
  override def enemyRaces  : Seq[Race] = Seq(Race.Unknown)
}
