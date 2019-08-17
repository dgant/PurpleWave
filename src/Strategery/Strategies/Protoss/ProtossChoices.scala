package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.WorkerRush
import Strategery.Strategies.Protoss.FFA._
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies._

object ProtossChoices {
  
  val pvr = Vector(
    PvROpen2Gate910,
    PvROpen2Gate1012,
    PvROpenZZCore,
    PvROpenZCoreZ,
    PvRDT,
    PvRProxy2Gate,
    PvR2Gate4Gate,
    PvRTinfoil2018,
    PvR1BaseDT,
    ProtossBigFFACarriers,
    ProtossHuntersFFAFFEGatewayCarriers,
    ProtossHuntersFFAFFEGateway,
    ProtossHuntersFFAAggroGateway,
    ProtossHuntersFFAFFEScoutReaver,
    ProtossHuntersFFAFFECarriers,
    PvE1BaseIslandCarrier,
    PvE15BaseIslandCarrier,
    PvE2BaseIslandCarrier,
    PvE3BaseIslandCarrier
  )
  
  /////////
  // PvT //
  /////////
  
  val pvtOpenersWithoutTransitions = Vector(
    PvTProxy2Gate,
    PvT13Nexus,
  )
  
  val pvtOpenersTransitioningFrom1GateCore = Vector(
    PvT21Nexus,
    PvT23Nexus,
    PvT28Nexus,
    PvT25BaseCarrier,
    PvT2GateRangeExpandCarrier,
    PvTDTExpand,
    PvT1GateRobo,
    PvT2GateRangeExpand,
    PvT2GateObserver,
    PvT1015Expand,
    PvT1015DT,
    PvTStove
  )

  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvT2GateRangeExpandCarrier,
    PvT2GateRangeExpand,
    PvT2GateObserver,
    PvT1015Expand,
    PvT1015DT
  )
  
  val pvtOpenersWithTransitions: Vector[Strategy] = (pvtOpenersTransitioningFrom1GateCore ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  val pvtOpenersAll: Vector[Strategy] = (pvtOpenersWithoutTransitions ++ pvtOpenersTransitioningFrom1GateCore ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // PvP //
  /////////
  
  val pvpOpenersWithoutTransitions = Vector(
    PvP2GateDTExpand,
    PvPProxy2Gate
  )
  
  val pvpOpenersTransitioningFrom2Gate = Vector(
    PvP2Gate1012,
    PvP2Gate1012Goon,
    PvP2Gate1012DT
  )
  
  val pvpOpenersTransitioningFrom1GateCore = Vector(
    PvP1GateReaverExpand,
    PvPGateGateRobo,
    PvP2GateGoon,
    PvP3GateRobo,
    PvP3GateGoon,
    PvP3GateGoonCounter,
    PvP4GateGoon
  )
  
  val pvpOpenersAll: Vector[Strategy] = (pvpOpenersWithoutTransitions ++ pvpOpenersTransitioningFrom2Gate ++ pvpOpenersTransitioningFrom1GateCore).distinct
  
  /////////
  // PvZ //
  /////////
  
  val pvzOpenersWithoutTransitions = Vector(
    PvZFFEConservative,
    PvZFFEEconomic,
    PvZGatewayFE,
    PvZProxy2Gate,
    PvZ1BaseForgeTech,
  )
  
  val pvzOpenersTransitioningFrom1GateCore = Vector(
    PvZ4Gate99,
    PvZ4Gate1012
  )

  val pvzOpenersTransitioningFrom2Gate = Vector(
    PvZ4Gate99,
    PvZ4Gate1012
  )
  
  val pvzMidgameTransitioningFromOneBase = Vector(
    PvZMidgame4Gate2Archon,
    PvZMidgame5GateGoon,
    PvZMidgame5GateGoonReaver
  )
  
  val pvzMidgameTransitioningFromTwoBases = Vector(
    PvZMidgame4Gate2Archon,
    PvZMidgame5GateGoon,
    PvZMidgame5GateGoonReaver,
    PvZMidgameCorsairReaverZealot,
    PvZMidgameCorsairReaverGoon,
    PvZMidgameBisu,
    PvZMidgameNeoBisu,
    PvZMidgameNeoNeoBisu
  )
  
  val pvzOpenersAll: Vector[Strategy] = (pvzOpenersWithoutTransitions ++ pvzOpenersTransitioningFrom1GateCore ++ pvzOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // All //
  /////////
  
  val gimmickOpeners = Vector(
    WorkerRush,
    PvTProxyDarkTemplar,
    PvE3BaseIslandCarrier,
    CarriersWithNoDefense,
    DarkArchonsWithNoDefense,
    ProtossBigFFACarriers,
    PvTReaverCarrierCheese)
  
  val standardOpeners: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
  
  val all: Vector[Strategy] = (gimmickOpeners ++ standardOpeners).distinct
}