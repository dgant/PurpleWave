package Strategery.Strategies.Terran

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvPStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Protoss)
}

abstract class TvPOpening extends TvPStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(Vector(
    TvP6Fac,
    TvPDeep4,
    TvP2Armory
  ))
}

object TvP1RaxFE extends TvPOpening
object TvPSiegeExpandBunker extends TvPOpening
object TvPFDStrong extends TvPOpening
object TvP2FacJoyO extends TvPOpening
object TvP6Fac extends TvPStrategy
object TvPDeep4 extends TvPStrategy
object TvP2Armory extends TvPStrategy