package Strategery.Strategies.Terran

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}

object TvPEarly14CC extends TvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    TvPMidgameBioTank
  ))
}

object TvPEarlyFDStrong extends TvPStrategy
object TvPMidgameBioTank extends TvPStrategy