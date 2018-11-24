package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.FFA._
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvR._
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
    PvEIslandPlasmaCarriers3Base,
    PvEIslandPlasmaCarriers1Base
  )
  
  /////////
  // PvT //
  /////////
  
  val pvtOpenersWithoutTransitions = Vector(
    PvTProxy2Gate,
    PvT13Nexus,
  )
  
  val pvtOpenersTransitioningFrom1Gate = Vector(
    PvT21Nexus,
    PvTFastCarrier,
    PvTDTExpand,
    PvT2GateObserver,
    PvT1015Expand,
    PvTEarly1015GateGoonDT,
    PvTEarly1GateStargateTemplar
  )
  
  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvT21Nexus,
    PvT2GateObserver,
    PvT1015Expand,
    PvTEarly1015GateGoonDT
  )
  
  val pvtOpenersWithTransitions: Vector[Strategy] = (pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  val pvtOpenersAll: Vector[Strategy] = (pvtOpenersWithoutTransitions ++ pvtOpenersTransitioningFrom1Gate ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // PvP //
  /////////
  
  val pvpOpenersWithoutTransitions = Vector(
    PvPOpen2GateDTExpand,
    PvPOpenProxy2Gate
  )
  
  val pvpOpenersTransitioningFrom2Gate = Vector(
    PvPOpen2Gate1012
  )
  
  val pvpOpenersTransitioningFrom1GateCore = Vector(
    PvPOpen1GateReaverExpand,
    PvPOpen2GateRobo,
    PvPOpen3GateGoon,
    PvPOpen4GateGoon
  )
  
  val pvpOpenersAll: Vector[Strategy] = (pvpOpenersWithoutTransitions ++ pvpOpenersTransitioningFrom2Gate ++ pvpOpenersTransitioningFrom1GateCore).distinct
  
  /////////
  // PvZ //
  /////////
  
  val pvzOpenersWithoutTransitions = Vector(
    PvZEarlyFFEConservative,
    PvZEarlyFFEEconomic,
    PvZEarlyFFEGreedy,
    PvZProxy2Gate
  )
  
  val pvzOpenersTransitioningFrom1Gate = Vector(
    PvZ4Gate99,
    PvZ4GateDragoonAllIn
  )
  
  val pvzOpenersTransitioningFrom2Gate = Vector(
    PvZ4Gate99,
    PvZ4GateDragoonAllIn
  )
  
  val pvzMidgameTransitioningFromOneBase = Vector(
    PvZMidgame4Gate2Archon,
    PvZMidgameGatewayAttack
  )
  
  val pvzMidgameTransitioningFromTwoBases = Vector(
    PvZMidgame4Gate2Archon,
    PvZMidgameGatewayAttack,
    PvZMidgameCorsairSpeedlot
  )
  
  val pvzOpenersAll: Vector[Strategy] = (pvzOpenersWithoutTransitions ++ pvzOpenersTransitioningFrom1Gate ++ pvzOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // All //
  /////////
  
  val gimmickOpeners = Vector(
    WorkerRush,
    MassPhotonCannon,
    ProxyDarkTemplar,
    PvEIslandPlasmaCarriers3Base,
    CarriersWithNoDefense,
    DarkArchonsWithNoDefense,
    ProtossBigFFACarriers,
    PvTReaverCarrierCheese)
  
  val standardOpeners: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
  
  val all: Vector[Strategy] = (gimmickOpeners ++ standardOpeners).distinct
}