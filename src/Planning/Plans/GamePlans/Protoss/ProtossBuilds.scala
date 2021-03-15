package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, Get}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  val TwoGate910 = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(Protoss.Gateway),
    Get(10,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(3,   Protoss.Zealot),
    Get(13,  Protoss.Probe),
    Get(5,   Protoss.Zealot))
  
  val TwoGate1012 = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(3,   Protoss.Zealot),
    Get(3,   Protoss.Pylon),
    Get(15,  Protoss.Probe),
    Get(5,   Protoss.Zealot),
    Get(17,  Protoss.Probe))
  
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
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Zealot),
    Get(16,  Protoss.Probe),
    Get(Protoss.CyberneticsCore))

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
    Get(20, Protoss.Probe),
    Get(3, Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(21, Protoss.Probe))
  
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
    Get(Protoss.DragoonRange),
    Get(Protoss.Dragoon),
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

  val PvT10Gate14Nexus = Vector[BuildRequest](
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(14, Protoss.Probe),
    Get(2, Protoss.Nexus),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(Protoss.Zealot),
    Get(18, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(19, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(20, Protoss.Probe),
    Get(2, Protoss.Gateway))

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
    Get(21, Protoss.Probe), // Modification; normally Probes #20+21 come after the Dragoons
    Get(2,  Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(23, Protoss.Probe),
    Get(3, Protoss.Pylon),
    Get(4, Protoss.Dragoon),
    Get(25, Protoss.Probe))

  
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

  val PvT24Nexus = Vector[BuildRequest] (
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
    Get(16, Protoss.Probe),
    Get(1,  Protoss.Dragoon),
    Get(17, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(19, Protoss.Probe),
    Get(2,  Protoss.Dragoon),
    Get(20, Protoss.Probe),
    Get(2,  Protoss.Nexus))

  val PvT32Nexus = Vector[BuildRequest] (
    Get(8,  Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(13, Protoss.Probe),
    Get(Protoss.Zealot),
    Get(14, Protoss.Probe),
    Get(Protoss.Assimilator),
    Get(15, Protoss.Probe),
    Get(2,  Protoss.Zealot),
    Get(16, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(3,  Protoss.Pylon), // 22/25
    Get(19, Protoss.Probe),
    Get(Protoss.Dragoon),
    Get(20, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(22, Protoss.Probe),
    Get(2,  Protoss.Dragoon),
    Get(4,  Protoss.Pylon), // 30/33
    Get(23, Protoss.Probe),
    Get(3,  Protoss.Dragoon),
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
  // See also https://www.youtube.com/watch?v=-vQmonHDbQU
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
    Get(21, Protoss.Probe),
    Get(3,  Protoss.Dragoon),
    Get(Protoss.Reaver),
    Get(22, Protoss.Probe),
    Get(4,  Protoss.Dragoon),
    Get(2, Protoss.Nexus),
    Get(5,  Protoss.Pylon),
    Get(23, Protoss.Probe),
    Get(Protoss.DragoonRange))
  
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
    Get(Protoss.Pylon),
    Get(Protoss.Forge),
    Get(10,  Protoss.Probe),
    Get(3,   Protoss.PhotonCannon),
    Get(12,  Protoss.Probe),
    Get(Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Gateway))
  
  val PvZFFE_Conservative = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(Protoss.Forge),
    Get(2,   Protoss.PhotonCannon))
  
  val PvZFFE_ForgeCannonNexus = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(11,  Protoss.Probe),
    Get(Protoss.Forge),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon),
    Get(2,   Protoss.Nexus))

  val PvZFFE_ForgeNexusCannon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(11,  Protoss.Probe),
    Get(Protoss.Forge),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(2,   Protoss.PhotonCannon))

  val PvZFFE_NexusGatewayForge = Vector[BuildRequest] (
    // This differs from https://liquipedia.net/starcraft/Forge_FE_(vs._Zerg) but should be safer
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
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
