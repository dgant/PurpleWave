package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, Get}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
  val TwoGate910 = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(10,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(1,   Protoss.Pylon),
    Get(3,   Protoss.Zealot))
  
  val TwoGate1012 = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(3,   Protoss.Zealot),
    Get(15,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(17,  Protoss.Probe),
    Get(5,   Protoss.Zealot))
  
  val ZCoreZ = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(15,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.Zealot))
  
  val ZZCore = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore))

  // As recommended by Antiga
  // The pro-style version gets the third Gateway a bit later, at 26
  // This accepts a slight worker cut, but with mineral locking should
  // be barely behind on Probes in exchange for a much faster third Gateway
  val ThreeGateGoon_NoZealot = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(17, Protoss.Probe),
    Get(1,  Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(19, Protoss.Probe),
    Get(2,  Protoss.Gateway),
    Get(20, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(3,  Protoss.Gateway),
    Get(3,  Protoss.Dragoon),
    Get(21, Protoss.Probe)
  )

  val ZCoreZTwoGateGoon = Vector[BuildRequest](
    Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2,  Protoss.Pylon),
      Get(15, Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(16, Protoss.Probe),
      Get(2,  Protoss.Zealot),
      Get(2, Protoss.Gateway),
      Get(17, Protoss.Probe),
      Get(Protoss.Dragoon),
      Get(18, Protoss.Probe),
      Get(3, Protoss.Pylon),
      Get(19, Protoss.Probe),
      Get(3, Protoss.Dragoon))

  val ThreeGateGoon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(15,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(18,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),
    Get(20,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(21,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(3,   Protoss.Gateway),
    Get(3,   Protoss.Dragoon),
    Get(4,   Protoss.Pylon),
    Get(6,   Protoss.Dragoon),
    Get(5,   Protoss.Pylon),
    Get(9,   Protoss.Dragoon)
  )

  val FourGateGoon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(15,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(18,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),
    Get(20,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(21,  Protoss.Probe),
    Get(2,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(4,   Protoss.Gateway),
    Get(3,   Protoss.Dragoon),
    Get(4,   Protoss.Pylon),
    Get(7,   Protoss.Dragoon),
    Get(5,   Protoss.Pylon),
    Get(11,   Protoss.Dragoon)
  )
  val NZ11Gas13Core = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon))
  
  val NZ12Gas14Core = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon))
  
  val TwoGate899 = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(3,   Protoss.Zealot))

  val TwoGate999 = Vector[BuildRequest] (
    Get(9,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(2,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(3,   Protoss.Zealot))
  
  val PvT1015GateGoon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(2,   Protoss.Pylon),
    Get(1,   Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(3,   Protoss.Dragoon),
    Get(3,   Protoss.Pylon))

  val PvT1015GateGoonDT = PvT1015GateGoon ++ Vector[BuildRequest] (
    Get(16,  Protoss.Probe),
    Get(Protoss.CitadelOfAdun),
    Get(5,   Protoss.Dragoon),
    Get(17,  Protoss.Probe),
    Get(Protoss.TemplarArchives),
    Get(18,  Protoss.Probe),
    Get(2,   Protoss.DarkTemplar),
    Get(19,  Protoss.Probe),
    Get(2,   Protoss.Nexus)
  )

  val PvT1015GateGoonExpand = PvT1015GateGoon ++ Vector[BuildRequest] (
    Get(5,   Protoss.Dragoon),
    Get(2,   Protoss.Nexus),
    Get(20,  Protoss.Probe)
  )
  
  val PvT12Nexus_2Gate = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(2,   Protoss.Gateway),
    Get(1,   Protoss.Zealot),
    Get(19,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(2,   Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(21,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(4,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(6,   Protoss.Dragoon)
  )

  val PvT13Nexus_GateCoreGateZ = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(1,  Protoss.Pylon),
    Get(13, Protoss.Probe),
    Get(2,  Protoss.Nexus),
    Get(14, Protoss.Probe),
    Get(1,  Protoss.Gateway),
    Get(15, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(17, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(2,  Protoss.Gateway),
    Get(Protoss.Zealot),
    Get(19, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(2,  Protoss.Dragoon),
    Get(21, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(3, Protoss.Pylon),
    Get(4, Protoss.Dragoon))

  
  val PvT13Nexus_GateCore = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore)
  )

  val PvT21Nexus = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(16,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(19,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),
    Get(2,   Protoss.Nexus))

  val PvT21Nexus_2GateRobo = PvT21Nexus ++ Vector[BuildRequest] (
    Get(2,   Protoss.Gateway),
    Get(2,   Protoss.Dragoon),
    Get(3,   Protoss.Pylon),
    Get(3,   Protoss.Dragoon),
    Get(20,  Protoss.Probe),
    Get(4,   Protoss.Dragoon),
    Get(Protoss.RoboticsFacility))

  val PvT23Nexus = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon), // Theoretically a 12 Pylon but putting it here avoids any queue nonsense
    Get(15,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(16,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(19,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(Protoss.Dragoon),
    Get(2,    Protoss.Nexus))

  val PvT28Nexus = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(1,  Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(1,  Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(1,  Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(1,  Protoss.CyberneticsCore),
    Get(15, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(16, Protoss.Probe),
    Get(1,  Protoss.Dragoon),
    Get(17, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(19, Protoss.Probe),
    Get(2,  Protoss.Dragoon),
    Get(20, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(21, Protoss.Probe),
    Get(3,  Protoss.Dragoon),
    Get(22, Protoss.Probe),
    Get(2,  Protoss.Nexus))

  val PvT2GateRangeExpand = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(17, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(18, Protoss.Probe),
    Get(2,  Protoss.Gateway),
    Get(20, Protoss.Probe),
    Get(2,  Protoss.Nexus),
    Get(2,  Protoss.Dragoon),
    Get(3,  Protoss.Pylon),
    Get(21, Protoss.Probe),
    Get(4,  Protoss.Dragoon))

  // Reference: Movie vs. Sharp: https://youtu.be/1pxD_HLpImg?t=4883
  val PvT1GateReaver = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(11, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(14, Protoss.Probe),
    Get(Protoss.Zealot),
    Get(2,  Protoss.Pylon),
    Get(16, Protoss.Probe),
    Get(Protoss.RoboticsFacility),
    Get(Protoss.Dragoon),
    Get(18, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(19, Protoss.Probe),
    Get(2,  Protoss.Dragoon),
    Get(20, Protoss.Probe),
    Get(Protoss.Shuttle),
    Get(4,  Protoss.Pylon),
    Get(Protoss.RoboticsSupportBay),
    Get(3,  Protoss.Dragoon),
    Get(21, Protoss.Probe),
    Get(Protoss.ShuttleSpeed),
    Get(22, Protoss.Probe),
    Get(5,  Protoss.Pylon),
    Get(Protoss.Reaver),
    Get(2, Protoss.Zealot),
    Get(25, Protoss.Probe),
    Get(2, Protoss.Nexus)
  )
  
  val PvT2GateObs = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.Dragoon),
    Get(17,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(20,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(2,   Protoss.Dragoon),
    Get(22,  Protoss.Probe),
    Get(Protoss.RoboticsFacility),
    Get(23,  Protoss.Probe),
    Get(3,   Protoss.Dragoon),
    Get(24,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(25,  Protoss.Probe),
    Get(4,   Protoss.Pylon),
    Get(4,   Protoss.Dragoon),
    Get(26,  Protoss.Probe),
    Get(5,   Protoss.Dragoon),
    Get(27,  Protoss.Probe),
    Get(Protoss.Observatory),
    Get(6,   Protoss.Dragoon),
    Get(28,  Protoss.Probe),
    Get(5,   Protoss.Pylon),
    Get(1,   Protoss.Observer),
    Get(29,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(8,   Protoss.Dragoon),
    Get(30,  Protoss.Probe))

  val PvTDTExpand_BeforeCitadel = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.Dragoon))

  val PvTDTExpand_WithCitadel = Vector[BuildRequest] (
    Get(Protoss.CitadelOfAdun),
    Get(21, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(Protoss.TemplarArchives),
    Get(23, Protoss.Probe),
    Get(2,  Protoss.Nexus),
    Get(25, Protoss.Probe),
    Get(1,  Protoss.DarkTemplar))

  val PvTDTExpand_WithoutCitadel = Vector[BuildRequest] (
    // Do stuff without building Citadel right away until we eject the scout
    Get(18, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(20, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(2,  Protoss.Dragoon),
    Get(23, Protoss.Probe),
    Get(3,  Protoss.Dragoon),
    Get(4,  Protoss.Pylon),
    Get(2,  Protoss.Nexus),
    Get(24, Protoss.Probe),
    Get(4,  Protoss.Dragoon),
    Get(25, Protoss.Probe),
    Get(Protoss.CitadelOfAdun),
    Get(2,  Protoss.Assimilator),
    Get(Protoss.TemplarArchives))
  
  //////////////////////
  // Protoss vs. Zerg //
  //////////////////////
  
  val PvZFFE_Vs4Pool = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(1,   Protoss.Forge),
    Get(10,  Protoss.Probe),
    Get(3,   Protoss.PhotonCannon),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Gateway))
  
  val PvZFFE_Conservative = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(2,   Protoss.PhotonCannon))
  
  val PvZFFE_ForgeCannonNexus = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus))

  val PvZFFE_ForgeNexusCannon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(2,   Protoss.PhotonCannon))

  val PvZFFE_NexusGatewayForge = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(2,   Protoss.Pylon),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.PhotonCannon),
    Get(17,  Protoss.Probe))

  val PvZFFE_NexusForgeCannons = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(1,   Protoss.Forge),
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon))

  val PvZFFE_GatewayForgeCannonsConservative = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(1,   Protoss.Forge),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.PhotonCannon),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Zealot))

  val PvZFFE_GatewayForgeCannonsEconomic = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.PhotonCannon),
    Get(2,   Protoss.Pylon),
    Get(2,   Protoss.Zealot),
    Get(13,  Protoss.Probe))

  val PvZFFE_GatewayNexusForge = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(17,  Protoss.Probe),
    Get(2,   Protoss.Nexus))
}
