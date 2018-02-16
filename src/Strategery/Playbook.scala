package Strategery

import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvT._
import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Terran.TvR.{TvR1Rax, TvRTinfoil}
import Strategery.Strategies.Terran.TvZ._

class EmptyPlaybook {
  
  lazy val forced   : Seq[Strategy] = Seq.empty
  lazy val disabled : Seq[Strategy] = Seq.empty
  
  val none: Seq[Strategy] = Seq.empty
  
  val strategyOrder: Seq[Strategy] = Seq.empty
}

object StrategyGroups {
  val bad = Vector[Strategy](
    WorkerRushLiftoff,
    TvR1Rax,
    TvRTinfoil,
    TvEProxy5Rax,
    TvEProxy8Fact,
    TvEProxyBBS,
    TvEMassGoliath,
    TvE2PortWraith,
    TvZEarlyCCFirst,
    TvZEarly1RaxGas,
    TvZEarly1RaxFEEconomic,
    TvZEarly1RaxFEConservative,
    TvZEarly2Rax,
    PvTEarly1GateProxy,
    PvT3BaseCorsair,
    MassPhotonCannon,
    CarriersWithNoDefense,
    ProxyDarkTemplar,
    PvZEarlyFFENexusFirst,
    PvZEarlyFFEGatewayFirst,
    PvZMidgame2Stargate
  )
}

class TestingPlaybook extends EmptyPlaybook {
  val strategiesToTest: Seq[Strategy] = Seq(PvTEarly1BaseCarrier)
  
  override lazy val forced: Seq[Strategy] = strategiesToTest
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val forced    : Seq[Strategy] = none
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.bad
}

object Playbook extends PurpleWavePlaybook {}
