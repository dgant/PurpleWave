package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
  val OpeningTwoGate910 = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(3,   Protoss.Zealot))
  
  val OpeningTwoGate1012 = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Zealot),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Zealot))
  
  val OpeningTwoGate1012Expand = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Zealot),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Zealot),
    RequestAtLeast(18,  Protoss.Probe))
  
  
  val OpeningZCoreZ = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot))
  
  val OpeningZZCore = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore))
  
  val Opening_4GateDragoon = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(20,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Gateway),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(7,   Protoss.Dragoon),
    RequestAtLeast(5,   Protoss.Pylon),
    RequestAtLeast(11,   Protoss.Dragoon)
  )
  val Opening_10Gate11Gas13Core = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon))
  
  val Opening_10Gate12Gas14Core = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon))
  
  val OpeningTwoGate99 = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(3,   Protoss.Zealot))
  
  val Opening10Gate15GateDragoons = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(3,   Protoss.Pylon))
  
  val Opening12Nexus = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestUpgrade(    Protoss.DragoonRange),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(6,   Protoss.Dragoon)
  )
  
  val Opening13Nexus_NoZealot_TwoGateways = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(2,   Protoss.Gateway),
    //Normally would get Zealot here
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestUpgrade(     Protoss.DragoonRange),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon)
  )
  
  val Opening13Nexus_NoZealot_OneGateCore = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore)
  )
  
  val Opening13Nexus = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(20,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(3,   Protoss.Pylon))
  
  val Opening21Nexus_Robo = Vector[BuildRequest] (
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(1,   Protoss.RoboticsFacility))
  
  val Opening2GateRoboPvT = Vector[BuildRequest] (
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.RoboticsFacility))
  
  val Opening2GateObserver = Vector[BuildRequest] (
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(17,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.RoboticsFacility),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(24,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(25,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(26,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Dragoon),
    RequestAtLeast(27,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Observatory),
    RequestAtLeast(6,   Protoss.Dragoon),
    RequestAtLeast(28,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Observer),
    RequestAtLeast(29,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Dragoon),
    RequestAtLeast(30,  Protoss.Probe))
  
  val OpeningDTExpand = Vector[BuildRequest] (
    // 2-Gate DT Expand based on PvP 2-Gate DT from http://wiki.teamliquid.net/starcraft/2_Gate_DT
    // We get gas/core faster because of mineral locking + later scout
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),           // 10
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),       // 11
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),            // 13 -- this is important to deny scouting
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),             // 20 = 18 + Z
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),           // 21 = 19 + Z
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),     // 24 = 20 + Z + D
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),           // 25 = 21 + Z + D
    RequestAtLeast(2,   Protoss.Gateway),           // 27 = 21 + Z + DD
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.TemplarArchives),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.DarkTemplar),
    RequestAtLeast(24,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus))
  
  //////////////////////
  // Protoss vs. Zerg //
  //////////////////////
  
  val FFE_Vs4Pool = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(2,   Protoss.PhotonCannon))
  
  val FFE_Conservative = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(2,   Protoss.PhotonCannon))
  
  val FFE_ForgeCannonNexus = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.PhotonCannon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(3,   Protoss.PhotonCannon),
    RequestAtLeast(2,   Protoss.Pylon))
  
  val FFE_NexusGatewayForge = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.PhotonCannon))
}
