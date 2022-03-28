package Strategery.Strategies.Terran.TvR

import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.{TerranChoices, TvT14CC}
import bwapi.Race

object TvR1Rax extends Strategy {
  
  override lazy val choices = Vector(
    TerranChoices.tvtOpeners.filterNot(TvT14CC==),
    TerranChoices.tvpOpeners,
    TerranChoices.tvzOpeners)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
