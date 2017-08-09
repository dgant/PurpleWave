package Strategery.Strategies.Terran.TvZ

import Strategery.Strategies.Strategy
import bwapi.Race

object TvZEarly1RaxFEConservative extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(
      TvZMidgameBio,
      TvZMidgameMech))
  
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}
