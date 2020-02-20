package Strategery

import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.TvR1Rax
import Strategery.Strategies.Terran.TvZ.TvZProxy8Fact
import Strategery.Strategies.Terran._
import Strategery.Strategies.Zerg._

class Playbook {
  val none: Seq[Strategy] = Seq.empty
  lazy val forced   : Seq[Strategy] = none
  lazy val disabled : Seq[Strategy] = none
  val strategyOrder: Seq[Strategy] = Vector(
  )
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
  def enemyName: String = With.enemy.name
  def respectOpponent: Boolean = true
  def respectMap: Boolean = true
  def respectHistory: Boolean = true
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,

    CarriersWithNoDefense,

    TvEWorkerRushLiftoff,
    TvR1Rax,
    TvZProxy8Fact,
    TvZ2RaxNuke,

    PvROpenZZCore,

    PvE15BaseIslandCarrier, // Disabled for Sparkle in TorchUp
    PvE2BaseIslandCarrier, // Disabled for Sparkle in TorchUp
    PvE3BaseIslandCarrier, // Disabled for Sparkle in TorchUp

    PvT1GateRobo,
    PvTProxyDarkTemplar,
    PvZLateGameCarrier, // Needs island tech
    PvZGatewayFE, // Execution needs work; in particular, Zealots need to protect cannons
    PvZMidgame4Gate2Archon,
    PvZMidgameNeoNeoBisu,
    PvZMidgameCorsairReaverGoon, // Too fragile
    PvZMidgameCorsairReaverZealot, // Too fragile; especially bad at dealing with Mutalisks

    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    ZvZ5PoolSunkens,

    PvTStove, // TODO: For ladder/fun play only
    PvT2GateObserver, // Experimenting with this so we can delete it
    PvT25BaseCarrier, // Experimenting with this so we can delete it
    PvT2GateRangeExpandCarrier, // Experimenting with this so we can delete it
    PvTReaverCarrierCheese,

    DarkArchonsWithNoDefense // Temporary for AIIDE testing
  )
}

class NormalPlaybook extends Playbook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
}

object TournamentPlaybook extends NormalPlaybook {
  //override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionTournament
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
}

object HumanPlaybook extends NormalPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionDynamic
}

class TestingPlaybook extends NormalPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(PvP2Gate1012Goon)
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
  override def respectOpponent: Boolean = false
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

object DefaultPlaybook extends NormalPlaybook {}
