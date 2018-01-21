package Strategery.Strategies.Terran.TvR

import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TerranChoices
import bwapi.Race

object TvR1RaxFE extends Strategy {
  
  override lazy val choices = Vector(
    TerranChoices.tvtOpeners,
    TerranChoices.tvpOpeners,
    TerranChoices.tvzOpeners)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
