package Strategery.Strategies.Terran

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvPStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Seq(Race.Terran)
  override def enemyRaces: Seq[Race] = Seq(Race.Protoss)
}

abstract class TvPOpening extends TvPStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(
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