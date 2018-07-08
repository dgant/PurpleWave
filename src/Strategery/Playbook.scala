package Strategery

import Lifecycle.With
import Strategery.Selection.{StrategySelectionCIG, StrategySelectionGreedy, StrategySelectionPolicy}
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
    PvZMidgameGatewayAttack,
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
    MassPhotonCannon,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    FivePoolProxySunkens,
    PvPOpen2GateRobo,
    PvPOpen4GateGoon
  )
}

class TestingPlaybook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(PvPOpen1GateReaverExpand, PvPLateGame2BaseReaverCarrier_SpecificOpponents)
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.disabled
}

class CIGPlaybook extends PurpleWavePlaybook {
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionCIG

  override lazy val disabled: Seq[Strategy] = StrategyGroups.disabled ++ Seq(
    PvZProxy2Gate
  )
}

class TestingCIGPlaybook extends CIGPlaybook {
  override def enemyName: String = "LetaBot"
}

object Playbook extends CIGPlaybook {}
