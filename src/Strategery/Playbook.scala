package Strategery

import Debugging.SimpleString
import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.{Sandbox, WorkerRushes}
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvR.TvR1Rax
import Strategery.Strategies.Zerg._

class Playbook extends SimpleString {
  lazy val disabled   : Seq[Strategy]           = Seq.empty
  def policy          : StrategySelectionPolicy = StrategySelectionGreedy()
  def enemyName       : String                  = With.enemy.name
  def respectMap      : Boolean                 = policy.respectMap
  def respectHistory  : Boolean                 = policy.respectHistory
}

object StrategyGroups {
  val disabled: Vector[Strategy] = WorkerRushes.all ++ Vector[Strategy](
    Sandbox,

    TvR1Rax, // Why is this disabled? Is it broken?

    PvE15BaseIslandCarrier, // Disabled for Sparkle in TorchUp
    PvE2BaseIslandCarrier, // Disabled for Sparkle in TorchUp

    //PvEStormNo, // Let's try storming again with the fixed storm micro
    PvT13Nexus, // Good Terran bots are bunker rushing this too effectively

    PvTProxy2Gate, // Proxy builds are temporarily broken due to new building placer
    PvPProxy2Gate, // Proxy builds are temporarily broken due to new building placer
    PvZProxy2Gate, // Proxy builds are temporarily broken due to new building placer

    // AIST4: This was always an exploit build due to inability to deny scouting; disable before probable removal
    PvP2Gate1012DT,
    // AIST4: This is horrible against the 4-Gate meta
    //PvP2GateGoon,
    // AIST4: Time to retire 2-gating except as win%rr safety pick. Maybe if we can catch an opponent who's skimping on zealots
    PvP2Gate1012Goon,
    // AIST4: Too many issues; too bad against 4-gate meta; needs polish we don't have time for
    PvP1ZealotExpand,

    PvZDT,       // Requires better micro on one base
    PvZCorsair,  // Requires better micro on one base
    PvZSpeedlot, // Requires better micro on one base

    // Experimentally reenabling these 12-19-2020
    //PvZGatewayFE, // Execution needs work; in particular, Zealots need to protect cannons
    //PvZMidgameCorsairReaverGoon, // Too fragile
    //PvZMidgameCorsairReaverZealot, // Too fragile; especially bad at dealing with Mutalisks

    ZvZ5PoolSunkens,         // Proxy builds are temporarily broken due to new building placer

    PvTStove, // TODO: For ladder/fun play only
  )
}

class NormalPlaybook extends Playbook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def policy: StrategySelectionPolicy = StrategySelectionGreedy()
}

object TournamentPlaybook extends NormalPlaybook {
  override def policy: StrategySelectionPolicy = StrategySelectionRandom
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
