package Strategery

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvP._
import Strategery.Strategies.Protoss.PvT._
import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.TvE._
import Strategery.Strategies.Zerg.Global._

class EmptyPlaybook {
  
  lazy val forced   : Seq[Strategy] = Seq.empty
  lazy val disabled : Seq[Strategy] = Seq.empty
  
  val none: Seq[Strategy] = Seq.empty
}

object StrategyGroups {
  
  val cheese = Vector[Strategy](
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEProxy5RaxAllIn,
    TvEMassMarineAllIn,
    PvTProxy2Gate,
    PvPOpenProxy2Gate,
    PvZProxy2Gate,
    PvTEarly1GateProxy,
    ProxyDarkTemplar,
    Zerg4PoolAllIn,
    ProxyHatchZerglings,
    ProxyHatchSunkens,
    ProxyHatchHydras
  )
  
  val bad = Vector[Strategy](
    PvTEarly1GateProxy,
    PvT3BaseCorsair,
    CarriersWithNoDefense,
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    PvZEarlyFFEGatewayFirst,
    PvZEarlyFFENexusFirst,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEProxy5RaxAllIn,
    TvEMassMarineAllIn,
    ProxyDarkTemplar
  )
}

class TestingPlaybook extends EmptyPlaybook {
  
  val strategiesToTest: Seq[Strategy] = Seq.empty
  
  //override lazy val forced: Seq[Strategy] = Seq(AllPvP, AllPvT, AllPvZ, TvTStandard, TvZStandard) ++ strategiesToTest
  override lazy val forced: Seq[Strategy] = strategiesToTest
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val forced    : Seq[Strategy] = none
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.bad
}

class PurpleCheesePlaybook extends EmptyPlaybook  {
  override lazy val forced: Seq[Strategy] = StrategyGroups.cheese
}

object Playbook extends PurpleWavePlaybook {

  val strategyOrder = Vector(
  )
  
}
