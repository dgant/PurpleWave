package Strategery

import Lifecycle.With
import Strategery.Selection.{StrategySelectionDynamic, _}
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvZ.TvZProxy8Fact
import Strategery.Strategies.Terran._
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
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,
    TvEWorkerRushLiftoff,
    TvETurtleMech,
    TvR1Rax,
    TvRTinfoil,
    TvZProxy8Fact,
    TvZ2RaxNuke,
    PvTProxyDarkTemplar,
    PvTProxy2Gate,
    PvPProxy2Gate,
    PvZProxy2Gate,
    PvP2Gate1012,
    PvP2GateRobo,
    PvPBlueStormReaverCarrier,
    PvZLateGameCarrier, // Needs island tech
    MassPhotonCannon,
    CarriersWithNoDefense,
    ZvZ5PoolSunkens,
    PvP1GateReaverExpand,
    PvPLateGameCarrier,
    PvT1GateRobo
  )
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionDynamic
}

class TestingPlaybook extends PurpleWavePlaybook {
  //override lazy val forced: Seq[Strategy] = Seq(PvZFFEEconomic, PvZMidgameCorsairReaverZealot, PvZMidgameCorsairReaverGoon, PvZMidgame5GateGoonReaver)
  override lazy val forced: Seq[Strategy] = Seq(ZvT2HatchLurker)
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
}

object Playbook extends PurpleWavePlaybook {}
