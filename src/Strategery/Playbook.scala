package Strategery

import Debugging.SimpleString
import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.Sandbox
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy

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

    PvTProxy2Gate,
    PvPProxy2Gate,
    PvT910,
    PvT1015,
    PvT1BaseReaver,
    PvT29Arbiter,
    PvT4Gate,
    PvTCustom,
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
