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
    //PvTProxy2Gate,
    PvT13Nexus,
    PvTZealotExpand
  )

  val pvtOpenersTransitioningFrom1GateCore = Vector(
    PvTRangeless,
    PvT28Nexus,
    PvTDT,
    PvT1015,
    PvT1BaseReaver,
    PvT29Arbiter,
    PvT4Gate
  )

  val pvtOpenersTransitioningFrom2Gate = Vector(
    PvTZZCoreZ, // Not really, but it should behave well
    PvT910,
    PvT4Gate,
    PvT1015
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
    PvPCoreExpand,
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
  )

  val pvzOpenersTransitioningFrom1GateCore = Vector(
    PvZ2022
  )

  val pvzOpenersTransitioningFrom2Gate = Vector(
    PvZ2022
  )

  val pvzOpenersAll: Vector[Strategy] = (pvzOpenersWithoutTransitions ++ pvzOpenersTransitioningFrom1GateCore ++ pvzOpenersTransitioningFrom2Gate).distinct
  
  /////////
  // All //
  /////////

  val all: Vector[Strategy] = (pvr ++ pvtOpenersAll ++ pvpOpenersAll ++ pvzOpenersAll).distinct
}