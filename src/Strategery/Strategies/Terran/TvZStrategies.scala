package Strategery.Strategies.Terran

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvZStrategy extends Strategy {
  override def ourRaces: Iterable[Race] = Vector(Race.Terran)
  override def enemyRaces: Iterable[Race] = Vector(Race.Zerg)
}

abstract class TvZMidgame extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZSK))
}

object TvZ8Rax extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZRaxCCAcademy, TvZRaxCCRax),
    Vector(TvZ5Rax, TvZ2RaxTech, TvZ2RaxTank))
}

object TvZ1RaxFE extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZRaxCCAcademy, TvZRaxCCRax),
    Vector(TvZ5Rax, TvZ2RaxTech, TvZ2RaxTank))
  override def responsesBlacklisted: Iterable[Fingerprint] = Seq(With.fingerprints.fourPool)
}

object TvZ2RaxAcademy extends TvZStrategy {
  override def choices: Iterable[Iterable[Strategy]] = Vector(
    Vector(TvZ5Rax))
}

object TvZRaxCCAcademy extends TvZStrategy
object TvZRaxCCRax extends TvZStrategy

object TvZ5Rax extends TvZMidgame
object TvZ2RaxTech extends TvZMidgame
object TvZ2RaxTank extends TvZMidgame

object TvZSK extends TvZStrategy

