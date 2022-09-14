package Strategery.Strategies.Terran

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Strategery.Strategies.Strategy
import bwapi.Race

abstract class TvZStrategy extends Strategy {
  override def ourRaces: Seq[Race] = Seq(Race.Terran)
  override def enemyRaces: Seq[Race] = Seq(Race.Zerg)
}

abstract class TvZMidgame extends TvZStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(
    Seq(TvZSK))
}

object TvZ8Rax extends TvZStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(
    Seq(TvZRaxCCAcademy, TvZRaxCCRax),
    Seq(TvZ5Rax, TvZ2RaxTech, TvZ2RaxTank))
}

object TvZ1RaxFE extends TvZStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(
    Seq(TvZRaxCCAcademy, TvZRaxCCRax),
    Seq(TvZ5Rax, TvZ2RaxTech, TvZ2RaxTank))
  override def responsesBlacklisted: Seq[Fingerprint] = Seq(With.fingerprints.fourPool)
}

object TvZ2RaxAcademy extends TvZStrategy {
  override def choices: Seq[Seq[Strategy]] = Seq(Seq(TvZ5Rax))
}

object TvZRaxCCAcademy extends TvZStrategy
object TvZRaxCCRax extends TvZStrategy

object TvZ5Rax extends TvZMidgame
object TvZ2RaxTech extends TvZMidgame
object TvZ2RaxTank extends TvZMidgame

object TvZSK extends TvZStrategy

