package Strategery.Strategies.Terran

import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

abstract class TvZMidgame extends Strategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZSK))
}

object TvZ1RaxFE extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZ5Rax, TvZ2RaxNuke, TvZ3RaxTank))
}

object TvZ5Rax extends TvZMidgame
object TvZ2RaxNuke extends TvZMidgame
object TvZ3RaxTank extends TvZMidgame

object TvZSK extends TvZStrategy

