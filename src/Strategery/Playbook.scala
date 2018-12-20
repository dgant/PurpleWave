package Strategery

import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ._
import Strategery.Strategies.Zerg._

class EmptyPlaybook {
  val none: Seq[Strategy] = Seq.empty
  lazy val forced   : Seq[Strategy] = none
  lazy val disabled : Seq[Strategy] = none
  val strategyOrder: Seq[Strategy] = Vector(
    PvT13NexusNZ,
    PvTDTExpand,
    PvT21Nexus,
    PvT2BaseCarrier,
    PvT2BaseArbiter,

    PvP2GateDTExpand,
    PvPProxy2Gate,
    PvP3GateGoon,
    PvP3GateRobo,

    PvZ4Gate99,
    PvZFFEEconomic,
    PvZGatewayFE,
    PvZMidgame5GateGoon,
    PvZMidgameNeoBisu
  )
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
  def enemyName: String = With.enemy.name
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,
    WorkerRushLiftoff,
    ProxyDarkTemplar,
    PvZFFEConservative,
    TvR1Rax,
    TvRTinfoil,
    TvEProxy5Rax,
    TvEProxy8Fact,
    TvEMassGoliath,
    TvE2PortWraith,
    TvTStandard,
    TvZEarlyCCFirst,
    TvZEarly1RaxGas,
    TvZEarly1RaxFEEconomic,
    TvZEarly1RaxFEConservative,
    TvZEarly2Rax,
    PvP2Gate1012,
    PvP2GateRobo,
    PvPLateGame2BaseReaverCarrier_SpecificOpponents,
    PvPLateGame2BaseReaverCarrier_SpecificMaps,
    MassPhotonCannon,
    CarriersWithNoDefense,
    FivePoolProxySunkens,
    PvP2Gate1012,
    PvP1GateReaverExpand,
    PvPLateGameCarrier,
    PvT1GateRobo
  )

  val sscaitWhitelisted = Vector[Strategy](
    // Terran
    PvT13NexusNZ,
    PvTDTExpand,
    PvT21Nexus,
    PvT28Nexus,
    PvT2GateRangeExpand,
    PvT2BaseCarrier,
    PvT2BaseArbiter,

    // Zerg
    //
    // Openers
    PvZ4Gate99,
    PvZGatewayFE,
    PvZFFEEconomic,
    // Midgames
    PvZMidgame5GateGoon,
    PvZMidgameNeoBisu,

    // Protoss
    //
    // Openers
    PvP3GateGoon,
    PvP3GateRobo,
    PvP2GateDTExpand,
    PvPProxy2Gate
  )
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
}

class SSCAITPlaybook extends PurpleWavePlaybook {
  override lazy val forced: Seq[Strategy] = StrategyGroups.sscaitWhitelisted
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionSSCAIT
}

class TestingPlaybook extends PurpleWavePlaybook {
  override lazy val forced: Seq[Strategy] = Seq(PvT13NexusNZ, PvT2BaseCarrier)
  //override lazy val forced: Seq[Strategy] = StrategyGroups.sscaitWhitelisted
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
}

object Playbook extends SSCAITPlaybook {}
