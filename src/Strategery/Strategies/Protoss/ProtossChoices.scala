package Strategery.Strategies.Protoss

import Strategery.Strategies._

object ProtossChoices {

  lazy val pve = Vector(
    PvECannonRushMaximum,
    PvECannonRushFE,
    PvEProxy2Gate
  )
  
  lazy val pvr = Vector(
    PvR2Gate4Gate,
    PvRDT,
    PvRFFE,
    PvRGFE,
    PvR1012,
    PvRZZCoreZ
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

  val vsTerran_From_NexusFirst = Vector(
    PvT13Nexus,
  )

  val vsTerran_From_FFE = Vector(
    PvT13Nexus,
  )

  val vsTerran_From_GFE = Vector(
    PvTZealotExpand
  )

  val vsTerran_From_GateCore = Vector(
    PvTRangeless,
    PvT28Nexus,
    PvTDT,
    PvT1015,
    PvT1BaseReaver,
    PvT29Arbiter,
    PvT4Gate,
    PvTSpeedlotDT,
    PvTGasSteal
  )

  val vsTerran_From_2Gate = Vector(
    PvTZZCoreZ, // Not really, but it should behave well
    PvT910,
    PvT4Gate,
    PvT1015,
    PvTSpeedlotDT
  )

  val vsTerran: Vector[Strategy] = (
    vsTerran_From_NexusFirst ++
    vsTerran_From_FFE ++
    vsTerran_From_GFE ++
    vsTerran_From_GateCore ++
    vsTerran_From_2Gate)
  .distinct

  /////////
  // PvP //
  /////////

  val vsProtoss_From_NexusFirst = Vector(
    PvPNexusFirst
  )

  val vsProtoss_From_FFE = Vector(
    PvPNexusFirst
  )

  val vsProtoss_From_GFE = Vector(
    PvPCoreExpand // Really we need a Zealot expand
  )

  val vsProtoss_From_2Gate = Vector(
    PvP1012
  )

  val vsProtoss_From_GateCore = Vector(
    PvPGateCore
  )

  val vsProtoss: Vector[Strategy] = (
    vsProtoss_From_NexusFirst ++
    vsProtoss_From_FFE ++
    vsProtoss_From_GFE ++
    vsProtoss_From_GateCore ++
    vsProtoss_From_2Gate)
  .distinct

  /////////
  // PvZ //
  /////////

  val vsZerg_From_NexusFirst = Vector(
    PvZFFE,
  )

  val vsZerg_From_FFE = Vector(
    PvZFFE,
  )

  val vsZerg_From_GFE = Vector(
    PvZGFE,
  )

  val vsZerg_From_GateCore = Vector(
    PvZ1BaseReactive,
    PvZ1GateCoreExpand,
    PvZ1GateCoreYoloSpeedlot,
    PvZ1GateCoreYoloDragoon
  )

  val vsZerg_From_2Gate = Vector(
    PvZ1BaseReactive,
    PvZ2Gate4Gate
  )

  val vsZerg: Vector[Strategy] = (
    vsZerg_From_NexusFirst ++
    vsZerg_From_FFE ++
    vsZerg_From_GFE ++
    vsZerg_From_GateCore ++
    vsZerg_From_2Gate)
  .distinct

  /////////
  // All //
  /////////

  val all: Vector[Strategy] = (pve ++ pvr ++ vsTerran ++ vsProtoss ++ vsZerg ++ oddball).distinct
}
