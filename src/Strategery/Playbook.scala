package Strategery

import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.{Sandbox, WorkerRushes}
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.TvR1Rax
import Strategery.Strategies.Zerg._

class Playbook {
  val none: Seq[Strategy] = Seq.empty
  lazy val disabled : Seq[Strategy] = none
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy()
  def enemyName: String = With.enemy.name
  def respectOpponent: Boolean = true
  def respectMap: Boolean = true
  def respectHistory: Boolean = true
}

object StrategyGroups {
  val disabled: Vector[Strategy] = WorkerRushes.all ++ Vector[Strategy](
    Sandbox,

    TvEWorkerRushLiftoff,
    TvR1Rax, // Why is this disabled? Is it broken?

    PvE15BaseIslandCarrier, // Disabled for Sparkle in TorchUp
    PvE2BaseIslandCarrier, // Disabled for Sparkle in TorchUp

    PvEStormNo, // Let's try storming again with the fixed storm micro
    PvT13Nexus, // Good Terran bots are bunker rushing this too effectively

    PvTProxy2Gate, // Proxy builds are temporarily broken due to new building placer
    PvPProxy2Gate, // Proxy builds are temporarily broken due to new building placer
    PvZProxy2Gate, // Proxy builds are temporarily broken due to new building placer

    PvZDT,       // Requires better micro on one base
    PvZCorsair,  // Requires better micro on one base
    PvZSpeedlot, // Requires better micro on one base

    // Experimentally reenabling these 12-19-2020
    //PvZGatewayFE, // Execution needs work; in particular, Zealots need to protect cannons
    //PvZMidgameCorsairReaverGoon, // Too fragile
    //PvZMidgameCorsairReaverZealot, // Too fragile; especially bad at dealing with Mutalisks

    ZvTProxyHatchZerglings, // Proxy builds are temporarily broken due to new building placer
    ZvTProxyHatchHydras,    // Proxy builds are temporarily broken due to new building placer
    ZvTProxyHatchSunkens,   // Proxy builds are temporarily broken due to new building placer
    ZvZ5PoolSunkens,         // Proxy builds are temporarily broken due to new building placer

    PvTStove, // TODO: For ladder/fun play only
  )
}

class NormalPlaybook extends Playbook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy()
}

object TournamentPlaybook extends NormalPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionTournament
}

object HumanPlaybook extends NormalPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionDynamic
}

object PretrainingPlaybook extends NormalPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionDynamic
}

class TestingPlaybook extends NormalPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
  override def respectOpponent: Boolean = false
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

object DefaultPlaybook extends NormalPlaybook {}
