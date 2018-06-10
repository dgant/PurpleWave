package Strategery

import Strategery.Selection.{StrategySelectionAIST1, StrategySelectionFree, StrategySelectionPolicy}
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
  val strategyOrder: Seq[Strategy] = none
  def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionFree
}

object StrategyGroups {
  val disabled = Vector[Strategy](
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
    PvPOpen3GateSpeedlots,
    PvZ4Gate99,
    MassPhotonCannon,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    FivePoolProxySunkens
  )
}

class TestingPlaybook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(ZvPTwoHatchMuta)
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.disabled
}

class AIST1Playbook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(
    ZergSparkle,
    ZvPNinePool,
    ZvPTwoHatchMuta,
    NinePoolMuta,
    NineHatchLings
  )
  
  override def strategySelectionPolicy: StrategySelectionPolicy = StrategySelectionAIST1
}

object Playbook extends AIST1Playbook {}
