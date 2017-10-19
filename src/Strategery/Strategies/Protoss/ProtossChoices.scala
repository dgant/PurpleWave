package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.Other.{CarriersFromAnIsland, ProtossFFACarriers}
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvP._
import Strategery.Strategies.Protoss.PvR._
import Strategery.Strategies.Protoss.PvT._
import Strategery.Strategies.Protoss.PvZ._
import Strategery.Strategies._

object ProtossChoices {
  
  val pvr = Vector(
    PvROpen2Gate910,
    PvROpen2Gate1012,
    PvROpenZZCore,
    PvROpenZCoreZ,
    PvROpenProxy2Gate,
    PvROpenTinfoil,
    ProtossFFACarriers,
    CarriersFromAnIsland
  )
  
  /////////
  // PvT //
  /////////
  
  val pvtOpenersWithoutTransitions = Vector(
    PvTProxy2Gate,
    PvTEarly1GateProxy,
    PvTEarlyNexusFirst
  )
  
  val pvtOpenersTransitioningFrom1Gate = Vector(
    PvTEarly1GateRange,
    PvTEarly1GateStargate,
    PvTEarly1GateStargateTemplar,
    PvTEarlyDTExpand,
    PvTEarly1015GateGoon
  )
  
  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvTEarly1015GateGoon
  )
  
  val pvtOpenersWithTransitions: Vector[Strategy] = (pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  val pvtOpenersAll: Vector[Strategy] = (pvtOpenersWithoutTransitions ++ pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // PvP //
  /////////
  
  val pvpOpenersWithoutTransitions = Vector(
    PvPOpenProxy2Gate,
    PvPOpen2GateDTExpand
  )
  
  val pvpOpenersWithTransitions = Vector(
    PvPOpen2GateRobo,
    PvPOpen3GateSpeedlots,
    PvPOpen4GateGoon
  )
  
  val pvpOpenersAll: Vector[Strategy] = (pvpOpenersWithoutTransitions ++ pvpOpenersWithTransitions).distinct
  
  /////////
  // PvZ //
  /////////
  
  val pvzOpenersWithoutTransitions = Vector(
    PvZEarlyFFEConservative,
    PvZEarlyFFEEconomic,
    PvZEarlyFFEGatewayFirst,
    PvZEarlyFFENexusFirst,
    PvZProxy2Gate
  )
  
  val pvzOpenersTransitioningFrom1Gate = Vector(
    PvZ4GateDragoonAllIn
  )
  
  val pvzOpenersTransitioningFrom2Gate = Vector(
    PvZEarly2Gate,
    PvZ4GateDragoonAllIn
  )
  
  val pvzMidgameTransitioningFromOneBase = Vector(
    PvZMidgame5GateDragoons
  )
  
  val pvzMidgameTransitioningFromTwoBases = Vector(
    PvZMidgame2Stargate,
    PvZMidgame5GateDragoons,
    PvZMidgameCorsairDarkTemplar,
    PvZMidgameCorsairReaver,
    PvZMidgameCorsairSpeedlot
  )
  
  val pvzOpenersAll: Vector[Strategy] = (pvzOpenersWithoutTransitions ++ pvzOpenersTransitioningFrom1Gate ++ pvzOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // All //
  /////////
  
  val gimmickOpeners = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    ProxyDarkTemplar,
    CarriersFromAnIsland,
    CarriersWithNoDefense,
    DarkArchonsWithNoDefense,
    ProtossFFACarriers)
  
  val standardOpeners: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
  
  val all: Vector[Strategy] = (gimmickOpeners ++ standardOpeners).distinct
}