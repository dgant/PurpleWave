package Strategery.Strategies.Protoss

import Strategery.Strategies._

object ProtossChoices {
  
  val pvr = Vector(
    PvR2Gate4Gate,
  )

  val oddball = Vector(
    ProtossFFA,
    ProtossFFAHunters,
    ProtossFFAMoney,
    PvTFPM,
    PvPFPM,
    PvZFPM,
    PvRFPM)

  /////////
  // PvT //
  /////////

  val vsTerran_NeverVsRandom = Vector(
    //PvTProxy2Gate,
    PvT13Nexus,
    PvTZealotExpand
  )

  val vsTerran_OpenersTransitioningFrom_1GateCore = Vector(
    PvTRangeless,
    PvT28Nexus,
    PvTDT,
    PvT1015,
    PvT1BaseReaver,
    PvT29Arbiter,
    PvT4Gate,
    PvTCustom,
    PvTGasSteal
  )

  val vsTerran_OpenersTransitioningFrom_2Gate = Vector(
    PvTZZCoreZ, // Not really, but it should behave well
    PvT910,
    PvT4Gate,
    PvT1015
  )

  val vsTerran_All: Vector[Strategy] = (vsTerran_NeverVsRandom ++ vsTerran_OpenersTransitioningFrom_1GateCore ++ vsTerran_OpenersTransitioningFrom_2Gate).distinct

  /////////
  // PvP //
  /////////

  val vsProtoss_NeverVsRandom = Vector(
    PvPProxy2Gate,
    PvPNexusFirst
  )

  val vsProtoss_OpenersTransitioningFrom_2Gate = Vector(
    PvPRobo,
    PvPDT,
    PvP3GateGoon,
    PvP4GateGoon
  )

  val vsProtoss_OpenersTransitioningFrom_1GateCore = Vector(
    PvPRobo,
    PvPDT,
    PvPCoreExpand,
    PvP3GateGoon,
    PvP4GateGoon
  )

  val vsProtoss_Openers: Vector[Strategy] = (vsProtoss_NeverVsRandom ++ vsProtoss_OpenersTransitioningFrom_2Gate ++ vsProtoss_OpenersTransitioningFrom_1GateCore).distinct

  /////////
  // PvZ //
  /////////

  val vsZerg_OpenersWithoutTransitions = Vector(
    PvZFFE,
  )

  val vsZerg_OpenersTransitioningFrom_1GateCore = Vector(
    PvZ1BaseReactive,
    PvZ1GateCore
  )

  val vsZerg_OpenersTransitioningFrom_2Gate = Vector(
    PvZ1BaseReactive,
    PvZ2Gate4Gate
  )

  val vsZerg_Openers: Vector[Strategy] = (vsZerg_OpenersWithoutTransitions ++ vsZerg_OpenersTransitioningFrom_1GateCore ++ vsZerg_OpenersTransitioningFrom_2Gate).distinct

  /////////
  // All //
  /////////

  val all: Vector[Strategy] = (pvr ++ vsTerran_All ++ vsProtoss_Openers ++ vsZerg_Openers ++ oddball).distinct
}