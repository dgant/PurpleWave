package Strategery

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvP._
import Strategery.Strategies.Protoss.PvT._
import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.Global._
import Strategery.Strategies.Terran.TvT.TvTStandard
import Strategery.Strategies.Terran.TvZ._
import Strategery.Strategies.Zerg.Global._

class EmptyPlaybook {
  
  lazy val forced   : Seq[Strategy] = Seq.empty
  lazy val disabled : Seq[Strategy] = Seq.empty
  
  val none = Seq.empty
}

object StrategyGroups {
  
  val cheese = Vector[Strategy](
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEProxy5RaxAllIn,
    TvEMassMarineAllIn,
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations,
    ProxyDarkTemplar,
    Zerg4PoolAllIn,
    ProxyHatchZerglings,
    ProxyHatchSunkens,
    ProxyHatchHydras
  )
  
  val bad = Vector[Strategy](
    WorkerRush3StartLocations,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvEProxy5RaxAllIn,
    TvEMassMarineAllIn,
    Proxy2Gate3StartLocations,
    ProxyDarkTemplar
  )
}

class TestingPlaybook extends EmptyPlaybook {
  
  val strategyToTest = PvPEarly1GateCore
  
  override lazy val forced: Seq[Strategy] = Seq(AllPvP, AllPvT, AllPvZ, TvTStandard, TvZStandard)
}

class PurpleWavePlaybook extends EmptyPlaybook {
  override lazy val forced    : Seq[Nothing] = none
  override lazy val disabled  : Seq[Strategy] = StrategyGroups.bad
}

class PurpleCheesePlaybook extends EmptyPlaybook {
  override lazy val forced: Seq[Strategy] = StrategyGroups.cheese
}

object Playbook extends PurpleWavePlaybook {
  
  //////////////////////
  // Experiment order //
  //////////////////////
  
  // Specify the order in which you want to try strategies vs. new opponents
  //
  // We're optimizing strategy selection for iterated round-robin play (ie. lots of games; goal is most total wins)
  // In that context, a win is a win regardless of who it's against.
  // Beating weaker opponents more consistently is worth losing exploratory games against stronger opponents
  // We want to try to 100-0 our opponents whenever possible.
  //
  // Particularly, we want to try builds that exploit opponent capabilities, rather than exploiting their build orders.
  // ie. if you can't defend a worker rush, it doesn't matter what build orders you have.
  //
  // So let's first try the strategies with the highest chance of 100-0ing based on exploiting capabilities.
  // We also want to alternate strategies.
  // If we don't win with 9-9 Gateways, we probably won't win with 10-12 Gateways either so try something else.
  //
  val strategyOrder = Vector(
    // Cool stuff
    TvEProxy5RaxAllIn,
    TvEProxyBBS2StartLocations,
    TvEProxyBBS3StartLocations,
    TvZStandard,
    TvZEarly1RaxFEEconomic,
    TvZEarly1RaxFEConservative,
    TvZEarlyCCFirst,
    TvZEarly2Rax,
    TvZEarly1RaxGas,
    ProxyDarkTemplar,
    ProxySunkens,
    ProxyHatchSunkens,
    ProxyHatchHydras,
    ProxyHatchZerglings,
    
      
    AllPvP,
    AllPvZ,
    AllPvT,
    PvTEarlyDTExpand,
    PvTEarly1015GateGoon,
    PvTEarly14Nexus,
    PvTEarly1GateRange,
    PvTEarly4GateAllIn,
    PvTLateCarriers,
    PvTLateMassGateway,
    PvTLateArbiters,
    PvPEarly1GateZZCore,
    PvPEarlyFE,
    PvPEarly2Gate1012,
    PvPEarly1GateCore,
    PvPEarlyFFE,
    PvPEarly2Gate910,
    PvPMidgameDarkTemplar,
    PvPMidgameObserverReaver,
    PvPMidgameFE,
    PvPMidgameReaver,
    PvPMidgameCarriers,
    PvPMidgame4GateGoon,
    PvZEarlyFFEConservative,
    PvZEarlyFFEEconomic,
    PvZEarlyFFEGatewayFirst,
    PvZEarlyFFENexusFirst,
    PvZEarly2Gate,
    PvZMidgameCorsairDarkTemplar,
    PvZMidgameCorsairSpeedlot,
    PvZMidgame5GateDragoons,
    PvZMidgameCorsairReaver
  )
  
  val strategyOrderRoundRobin = Vector(
    /*
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations,
    PvREarly2Gate910,
    PvREarly2Gate910AtNatural,
    PvREarly2Gate1012,
    PvTEarlyDTExpand,
    PvTEarly4GateAllIn,
    PvTEarly14Nexus,
    PvTEarly1015GateGoon,
    PvTEarly1GateRange,
    PvTLateMassGateway,
    PvTLateCarriers,
    PvTLateArbiters,
    PvPEarly2Gate910,
    PvPEarlyFE,
    PvPEarly1GateZZCore,
    PvPEarly2Gate1012,
    PvPEarly1GateCore,
    PvPEarlyFFE,
    PvPMidgameDarkTemplar,
    PvPMidgameReaver,
    PvPMidgameObserverReaver,
    PvPMidgameFE,
    PvPMidgame4GateGoon,
    PvPMidgameCarriers,
    PvZEarlyZealotAllIn,
    PvZEarlyFFEHeavy,
    PvZEarly2Gate,
    PvZEarlyFFELight,
    PvZMidgame5GateDragoons,
    PvZMidgameCorsairReaver,
    PvZMidgameCorsairSpeedlot,
    PvZMidgameCorsairCarrier
    */
  )
}
