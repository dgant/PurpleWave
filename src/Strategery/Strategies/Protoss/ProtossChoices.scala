package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.FFA._
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvP.{PvPOpen1015GateDTs, PvPOpenProxy2Gate, _}
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
    ProtossBigFFACarriers,
    ProtossHuntersFFAFFEGatewayCarriers,
    ProtossHuntersFFAFFEGateway,
    ProtossHuntersFFAAggroGateway,
    ProtossHuntersFFAFFEScoutReaver,
    ProtossHuntersFFAFFECarriers,
    CarriersFromAnIsland
  )
  
  /////////
  // PvT //
  /////////
  
  val pvtOpenersWithoutTransitions = Vector(
    PvTProxy2Gate,
    PvTEarly1GateProxy,
    PvTEarlyNexusFirst,
    PvTEarlyDTDrop
  )
  
  val pvtOpenersTransitioningFrom1Gate = Vector(
    PvTEarly1GateRange,
    PvTEarly1GateStargate,
    PvTEarly1GateReaver,
    PvTEarly1GateStargateTemplar,
    PvTEarlyDTExpand,
    PvTEarly1015GateGoonDT,
    PvTEarly1015GateGoonExpand,
    PvTEarly1015GateGoonPressure,
    PvTEarly4Gate
  )
  
  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvTEarly1015GateGoonDT,
    PvTEarly1015GateGoonExpand,
    PvTEarly1015GateGoonPressure,
    PvTEarly4Gate
  )
  
  val pvtOpenersWithTransitions: Vector[Strategy] = (pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  val pvtOpenersAll: Vector[Strategy] = (pvtOpenersWithoutTransitions ++ pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // PvP //
  /////////
  
  val pvpOpenersWithoutTransitions = Vector(
    PvPOpen2GateDTExpand,
    PvPOpen2GateRobo,
    PvPOpen12Nexus5Zealot,
    PvPOpen1015GateDTs,
    PvPOpen1015GateGoonExpand,
    PvPOpen1015GateReaverExpand,
    PvPOpenProxy2Gate
  )
  
  val pvpOpenersTransitioningFrom2Gate = Vector(
    PvPOpen2Gate1012,
    PvPOpen3GateSpeedlots
  )
  
  val pvpOpenersTransitioningFrom1GateCore = Vector(
    PvPOpen1GateGoonExpand,
    PvPOpen1GateReaverExpand,
    PvPOpen4GateGoon
  )
  
  val pvpOpenersAll: Vector[Strategy] = (pvpOpenersWithoutTransitions ++ pvpOpenersTransitioningFrom2Gate ++ pvpOpenersTransitioningFrom1GateCore).distinct
  
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
    ProtossBigFFACarriers)
  
  val standardOpeners: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
  
  val all: Vector[Strategy] = (gimmickOpeners ++ standardOpeners).distinct
}