package Strategery

import Lifecycle.With
import Strategery.Selection._
import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvR.{PvROpen2Gate1012, PvROpen2Gate910}
import Strategery.Strategies.Protoss.{PvPOpen3GateGoon, _}
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
    PvEIslandPlasmaCarriers1Base,
    PvPLateGame2BaseReaverCarrier_SpecificMaps,
    PvTEarly1015GateGoonDT,
    PvT2GateObserver,
    PvT13Nexus,
    PvT21Nexus,
    PvT3BaseArbiter,
    PvT2BaseCarrier,
    PvPOpen2Gate1012,
    PvPOpen2GateDTExpand,
    PvPOpen3GateGoon,
    PvZ4Gate99,
    PvZEarlyFFEEconomic,
    PvZMidgame5GateGoon,
    PvROpen2Gate910,
    PvROpen2Gate1012
  )
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionGreedy
  def enemyName: String = With.enemy.name
}

object StrategyGroups {
  val disabled = Vector[Strategy](
    WorkerRush,
    WorkerRushLiftoff,
    PvZEarlyFFEConservative,
    PvZMidgameNeoBisu,
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
    PvPLateGame2BaseReaverCarrier_SpecificOpponents,
    PvPLateGame2BaseReaverCarrier_SpecificMaps,
    MassPhotonCannon,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    FivePoolProxySunkens,
    PvPOpen2GateRobo,
    PvPOpen2Gate1012,
    PvPOpen1GateReaverExpand,
    PvPLateGameCarrier
  )
}

class TestingPlaybook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(PvPOpen3GateRobo)
  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionRandom
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.disabled
}

class SSCAITPlaybook extends PurpleWavePlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionSSCAIT
}

object Playbook extends TestingPlaybook {}
