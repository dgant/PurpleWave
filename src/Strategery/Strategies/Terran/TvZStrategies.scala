package Strategery.Strategies.Terran

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

object TvZ1RaxFE extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZSK))
}

object TvZSK extends TvZStrategy
