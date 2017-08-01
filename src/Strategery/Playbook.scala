package Strategery

import Strategery.Strategies.Options.AllRaces.{Proxy2Gate2StartLocations, Proxy2Gate3StartLocations, WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.Global.{AllPvP, AllPvT, AllPvZ}
import Strategery.Strategies.Options.Protoss.PvP._
import Strategery.Strategies.Options.Protoss.PvT._
import Strategery.Strategies.Options.Protoss.PvZ._
import Strategery.Strategies.Strategy

object Playbook {
  
  // Don't use these.
  //
  val disabled = Vector[Strategy](
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate3StartLocations
  )
  
  // Use these whenever possible.
  //
  val forced = Vector[Strategy](
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations
    //AllPvZ
    
    /*
    AllPvP,
    PvPEarly1GateCore,
    PvPMidgame4GateGoon,
    AllPvT,
    PvTEarly1015GateGoon,
    AllPvZ,
    PvZEarlyFFELight,
    PvZMidgame5GateDragoons
    */
  )
  
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
