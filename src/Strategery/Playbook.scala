package Strategery

import Strategery.Strategies.Options.AllRaces.{Proxy2Gate2StartLocations, WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.PvP._
import Strategery.Strategies.Options.Protoss.PvR.{PvREarly2Gate1012, PvREarly2Gate99, PvREarly2Gate99AtNatural}
import Strategery.Strategies.Options.Protoss.PvT._
import Strategery.Strategies.Options.Protoss.PvZ._

object Playbook {
  
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
  
  val strategyOrder = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    PvREarly2Gate99,
    PvREarly2Gate99AtNatural,
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
  )
}
