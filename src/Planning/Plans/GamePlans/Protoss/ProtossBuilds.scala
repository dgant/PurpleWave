package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, Get}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
  val OpeningTwoGate910 = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
  
  val OpeningTwoGate1012 = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(3,   Protoss.Zealot),
    Get(3,   Protoss.Pylon),
    Get(17,  Protoss.Probe),
    Get(5,   Protoss.Zealot))
  
  val OpeningZCoreZ = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(16,  Protoss.Probe),
    Get(2,   Protoss.Zealot))
  
  val OpeningZZCore = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
  val Opening_3GateDragoon = Vector[BuildRequest] (
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

  val Opening_4GateDragoon = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
    Get(1,   Protoss.CyberneticsCore),
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
  val Opening_10Gate11Gas13Core = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
  
  val Opening_10Gate12Gas14Core = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
  
  val OpeningTwoGate99 = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.Pylon),
    Get(3,   Protoss.Zealot))
  
  val Opening10Gate15GateDragoons = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(11,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(13,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(2,   Protoss.Pylon),
    Get(1,   Protoss.Dragoon),
    Get(Protoss.DragoonRange),
    Get(3,   Protoss.Dragoon),
    Get(3,   Protoss.Pylon))
  
  val Opening10Gate15GateDragoonDT = Opening10Gate15GateDragoons ++ Vector[BuildRequest] (
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.CitadelOfAdun),
    Get(5,   Protoss.Dragoon),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.TemplarArchives),
    Get(18,  Protoss.Probe),
    Get(2,   Protoss.DarkTemplar),
    Get(19,  Protoss.Probe),
    Get(2,   Protoss.Nexus)
  )
  
  val Opening10Gate15GateExpand = Opening10Gate15GateDragoons ++ Vector[BuildRequest] (
    Get(5,   Protoss.Dragoon),
    Get(2,   Protoss.Nexus),
    Get(20,  Protoss.Probe)
  )
  
  val Opening12Nexus = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
    Get(    Protoss.DragoonRange),
    Get(21,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(4,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(6,   Protoss.Dragoon)
  )
  
  val Opening13Nexus_NoZealot_TwoGateways = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(2,   Protoss.Gateway),
    //Normally would get Zealot here
    Get(19,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(2,   Protoss.Dragoon),
    Get(     Protoss.DragoonRange),
    Get(21,  Protoss.Probe),
    Get(3,   Protoss.Pylon),
    Get(4,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(3,   Protoss.Pylon)
  )
  
  val Opening13Nexus_NoZealot_OneGateCore = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
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
  
  val Opening13Nexus_NoZealot_2Gate = Vector[BuildRequest] (
    Get(1,   Protoss.Nexus),
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(1,   Protoss.Gateway),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(17,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(2,   Protoss.Gateway),
    Get(1,   Protoss.Dragoon),
    Get(19,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(3,   Protoss.Dragoon),
    Get(20,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(3,   Protoss.Pylon))
  
  val Opening21Nexus_Robo = Vector[BuildRequest] (
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
    Get(2,   Protoss.Nexus),
    Get(2,   Protoss.Gateway),
    Get(2,   Protoss.Dragoon),
    Get(3,   Protoss.Pylon),
    Get(3,   Protoss.Dragoon),
    Get(20,  Protoss.Probe),
    Get(4,   Protoss.Dragoon),
    Get(1,   Protoss.RoboticsFacility))

  val Opening21Nexus = Vector[BuildRequest] (
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

  val Opening28Nexus = Vector[BuildRequest] (
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
    Get(2,  Protoss.Nexus),
  )

  // Reference: Movie vs. Sharp: https://youtu.be/1pxD_HLpImg?t=4883
  val Opening1GateReaverPvT = Vector[BuildRequest] (
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
    Get(Protoss.Reaver),
    Get(22, Protoss.Probe),
    Get(5,  Protoss.Pylon),
    Get(2, Protoss.Zealot),
    Get(25, Protoss.Probe),
    Get(2, Protoss.Nexus)
  )
  
  val Opening2GateRoboPvT = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.Pylon),
    Get(17,  Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(18,  Protoss.Probe),
    Get(2,   Protoss.Gateway),
    Get(20,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(2,   Protoss.Dragoon),
    Get(3,   Protoss.Pylon),
    Get(21,  Protoss.Probe),
    Get(4,   Protoss.Dragoon),
    Get(23,  Protoss.Probe),
    Get(1,   Protoss.RoboticsFacility))
  
  val Opening2GateObserver = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Assimilator),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.CyberneticsCore),
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
    Get(1,   Protoss.RoboticsFacility),
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
    Get(1,   Protoss.Observatory),
    Get(6,   Protoss.Dragoon),
    Get(28,  Protoss.Probe),
    Get(5,   Protoss.Pylon),
    Get(1,   Protoss.Observer),
    Get(29,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(8,   Protoss.Dragoon),
    Get(30,  Protoss.Probe))

  val OpeningDTExpand_BeforeCitadel = Vector[BuildRequest] (
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

  val OpeningDTExpand_AfterCitadel = Vector[BuildRequest] (
    Get(Protoss.CitadelOfAdun),
    Get(21, Protoss.Probe),
    Get(3,  Protoss.Pylon),
    Get(Protoss.TemplarArchives),
    Get(23, Protoss.Probe),
    Get(2,  Protoss.Nexus),
    Get(25, Protoss.Probe),
    Get(1,  Protoss.DarkTemplar))

  val OpeningDTExpand_WithoutCitadel = Vector[BuildRequest] (
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
  
  val FFE_Vs4Pool = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(1,   Protoss.Forge),
    Get(10,  Protoss.Probe),
    Get(3,   Protoss.PhotonCannon),
    Get(12,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(13,  Protoss.Probe),
    Get(2,   Protoss.Gateway))
  
  val FFE_Conservative = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(9,   Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(2,   Protoss.PhotonCannon))
  
  val FFE_ForgeCannonNexus = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon),
    Get(14,  Protoss.Probe),
    Get(2,   Protoss.Nexus))

  val FFE_ForgeNexusCannon = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(10,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(2,   Protoss.PhotonCannon))

  val FFE_NexusForgeCannons = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(15,  Protoss.Probe),
    Get(2,   Protoss.PhotonCannon))

  val FFE_NexusGatewayForge = Vector[BuildRequest] (
    Get(8,   Protoss.Probe),
    Get(1,   Protoss.Pylon),
    Get(12,  Protoss.Probe),
    Get(2,   Protoss.Nexus),
    Get(14,  Protoss.Probe),
    Get(1,   Protoss.Gateway),
    Get(15,  Protoss.Probe),
    Get(1,   Protoss.Forge),
    Get(16,  Protoss.Probe),
    Get(1,   Protoss.Zealot),
    Get(2,   Protoss.PhotonCannon))

  val FFE_GatewayForgeCannonsConservative = Vector[BuildRequest] (
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

  val FFE_GatewayForgeCannonsEconomic = Vector[BuildRequest] (
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

  val FFE_GatewayNexusForge = Vector[BuildRequest] (
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
