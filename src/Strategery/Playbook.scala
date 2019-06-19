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
import Strategery.Strategies.Utility.EvELogMapInfo
import Strategery.Strategies.Zerg._

class EmptyPlaybook {
  val none: Seq[Strategy] = Seq.empty
  lazy val forced   : Seq[Strategy] = none
  lazy val disabled : Seq[Strategy] = none
  val strategyOrder: Seq[Strategy] = Vector(
    PvT13Nexus,
    PvTDTExpand,
    PvT21Nexus,
    PvT23Nexus,
    PvT28Nexus,
    PvT2GateRangeExpand,

    PvT2BaseCarrier,
    PvT2BaseArbiter,

    PvP2Gate1012Goon,
    PvP2GateDTExpand,
    PvP3GateGoon,
    PvP3GateRobo,

    PvZ4Gate99,
    PvZFFEEconomic,
    PvZGatewayFE,
    PvZMidgame5GateGoon,
    PvZMidgameNeoBisu,
  )
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
  def enemyName: String = With.enemy.name
  def respectMap: Boolean = true
  def respectHistory: Boolean = true
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,
    TvEWorkerRushLiftoff,
    TvR1Rax,
    TvZProxy8Fact,
    TvZ2RaxNuke,
    PvTProxyDarkTemplar,
    PvP2Gate1012,
    ZvTProxyHatchZerglings,
    ZvTProxyHatchHydras,
    ZvTProxyHatchSunkens,
    PvZLateGameCarrier, // Needs island tech
    CarriersWithNoDefense,
    ZvZ5PoolSunkens,
    PvP1GateReaverExpand,
    PvT1GateRobo
  )
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
}

class CIGPlaybook extends PurpleWavePlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionCIG
}

class TestingPlaybook extends PurpleWavePlaybook {
  override lazy val forced: Seq[Strategy] = Seq(EvELogMapInfo)
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

class ForcedPlaybook extends EmptyPlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = new StrategySelectionFixed(PvPProxy2Gate)
  override def respectMap: Boolean = false
  override def respectHistory: Boolean = false
}

object Playbook extends CIGPlaybook {}
