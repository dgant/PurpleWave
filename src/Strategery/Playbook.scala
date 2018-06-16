package Strategery

import Strategery.Selection.{StrategySelectionFree, StrategySelectionPolicy, StrategySelectionShowmatch}
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
    PvZ4Gate99,
    MassPhotonCannon,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    FivePoolProxySunkens
  )
}

class TestingPlaybook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = Seq(ZvPThirteenPoolMuta)
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.disabled
}

class AISTShowmatchPlaybook extends EmptyPlaybook {
  override val strategySelectionPolicy = StrategySelectionShowmatch
}

object Playbook extends AISTShowmatchPlaybook {}
