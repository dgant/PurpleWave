package Strategery.Strategies.Protoss

import Strategery.Strategies.Protoss.FFA._
import Strategery.Strategies._

object ProtossChoices {
  
  val pvr = Vector(
    PvROpen2Gate910,
    PvROpen2Gate1012,
    PvROpenZCoreZ,
    PvRProxy2Gate,
    PvR2Gate4Gate,
    PvR1BaseDT,
    ProtossFFA,
    ProtossHuntersFFA,
  )
  
  /////////
  // PvT //
  /////////
  
  val pvtOpenersTransitioningFromNothing = Vector(
    PvTProxy2Gate,
    PvT13Nexus,
  )

  val pvtOpenersTransitioningFrom1GateCore = Vector(
    PvT24Nexus,
    PvTZZCoreZ,
    PvTDTExpand,
    PvT1GateReaver,
    PvT2GateRangeExpand,
    PvT1015Expand,
    PvT1015DT,
    PvTStove
  )

  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvT2GateRangeExpand,
    PvT1015Expand,
    PvT1015DT
  )

  val pvtOpenersAll: Vector[Strategy] = (pvtOpenersTransitioningFromNothing ++ pvtOpenersTransitioningFrom1GateCore ++ pvtOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // PvP //
  /////////

  val pvpOpenersWithoutTransitions = Vector(
    PvPProxy2Gate
  )
  
  val pvpOpenersTransitioningFrom2Gate = Vector(
    PvPRobo,
    PvPDT,
    PvP3GateGoon,
    PvP4GateGoon
  )
  
  val pvpOpenersTransitioningFrom1GateCore = Vector(
    PvPRobo,
    PvPDT,
    PvP3GateGoon,
    PvP4GateGoon
  )
  
  val pvpOpenersAll: Vector[Strategy] = (pvpOpenersWithoutTransitions ++ pvpOpenersTransitioningFrom2Gate ++ pvpOpenersTransitioningFrom1GateCore).distinct
  
  /////////
  // PvZ //
  /////////
  
  val pvzOpenersWithoutTransitions = Vector(
    PvZFFE,
    PvZGatewayFE,
    PvZProxy2Gate,
    PvZ1BaseForgeTech,
  )

  val pvzOpenersTransitioningFrom1GateCore = Vector(
    PvZ2GateFlex
  )

  val pvzOpenersTransitioningFrom2Gate = Vector(
    PvZ2GateFlex
  )
  
  val pvzMidgameTransitioningFromTwoBases = Vector(
    PvZMidgame5GateGoon,
    PvZMidgame5GateGoonReaver,
    PvZMidgameCorsairReaverGoon,
    PvZMidgameBisu,
  )
  
  val pvzOpenersAll: Vector[Strategy] = (pvzOpenersWithoutTransitions ++ pvzOpenersTransitioningFrom1GateCore ++ pvzOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // All //
  /////////

  val all: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
}