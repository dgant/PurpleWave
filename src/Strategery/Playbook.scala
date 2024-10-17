package Strategery

import Debugging.SimpleString
import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.Sandbox
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvR.TvR1Rax

class Playbook extends SimpleString {
  lazy val disabled   : Seq[Strategy]           = Seq.empty
  def policy          : StrategySelectionPolicy = StrategySelectionGreedy()
  def enemyName       : String                  = With.enemy.name
  def respectMap      : Boolean                 = policy.respectMap
  def respectHistory  : Boolean                 = policy.respectHistory
}

object StrategyGroups {
  val disabled: Vector[Strategy] = Vector[Strategy](
    Sandbox,

    TvR1Rax, // Why is this disabled? Is it broken?

    PvRProxy2Gate,
    PvTProxy2Gate,
    PvPProxy2Gate,

    PvZGatewayFE,
  )
}

class NormalPlaybook extends Playbook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def policy: StrategySelectionPolicy = StrategySelectionGreedy()
}

object TournamentPlaybook extends NormalPlaybook {
  override def policy: StrategySelectionPolicy = StrategySelectionTournament
}

object HumanPlaybook extends NormalPlaybook {
  override def policy: StrategySelectionPolicy = StrategySelectionDynamic
}

object PretrainingPlaybook extends NormalPlaybook {
  override def policy: StrategySelectionPolicy = StrategySelectionDynamic
}

class TestingPlaybook extends NormalPlaybook {
  override def policy: StrategySelectionPolicy = StrategySelectionRandom
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

object DefaultPlaybook extends NormalPlaybook {}
