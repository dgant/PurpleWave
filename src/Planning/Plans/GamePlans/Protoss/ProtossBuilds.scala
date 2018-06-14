package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetUpgrade}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
  val OpeningTwoGate910 = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(9,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(3,   Protoss.Zealot))
  
  val OpeningTwoGate1012 = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Zealot),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(5,   Protoss.Zealot))
  
  val OpeningTwoGate1012Expand = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Zealot),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(5,   Protoss.Zealot),
    GetAtLeast(18,  Protoss.Probe))
  
  
  val OpeningZCoreZ = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot))
  
  val OpeningZZCore = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore))
  
  val Opening_4GateDragoon = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),
    GetAtLeast(20,  Protoss.Probe),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Dragoon),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Gateway),
    GetAtLeast(3,   Protoss.Dragoon),
    GetAtLeast(4,   Protoss.Pylon),
    GetAtLeast(7,   Protoss.Dragoon),
    GetAtLeast(5,   Protoss.Pylon),
    GetAtLeast(11,   Protoss.Dragoon)
  )
  val Opening_10Gate11Gas13Core = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon))
  
  val Opening_10Gate12Gas14Core = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon))
  
  val OpeningTwoGate99 = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(9,   Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(3,   Protoss.Zealot))
  
  val Opening10Gate15GateDragoons = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.Dragoon),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(3,   Protoss.Dragoon),
    GetAtLeast(3,   Protoss.Pylon))
  
  val Opening10Gate15GateDragoonDT = Opening10Gate15GateDragoons ++ Vector[BuildRequest] (
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CitadelOfAdun),
    GetAtLeast(5,   Protoss.Dragoon),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.TemplarArchives),
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(2,   Protoss.DarkTemplar),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus)
  )
  
  val Opening10Gate15GateExpand = Opening10Gate15GateDragoons ++ Vector[BuildRequest] (
    GetAtLeast(5,   Protoss.Dragoon),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(20,  Protoss.Probe)
  )
  
  val Opening12Nexus = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(2,   Protoss.Dragoon),
    GetUpgrade(    Protoss.DragoonRange),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(4,   Protoss.Dragoon),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(6,   Protoss.Dragoon)
  )
  
  val Opening13Nexus_NoZealot_TwoGateways = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(2,   Protoss.Gateway),
    //Normally would get Zealot here
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(2,   Protoss.Dragoon),
    GetUpgrade(     Protoss.DragoonRange),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(4,   Protoss.Dragoon),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon)
  )
  
  val Opening13Nexus_NoZealot_OneGateCore = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore)
  )
  
  val Opening13Nexus = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(1,   Protoss.Dragoon),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(3,   Protoss.Dragoon),
    GetAtLeast(20,  Protoss.Probe),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(3,   Protoss.Pylon))
  
  val Opening21Nexus_Robo = Vector[BuildRequest] (
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(16,  Protoss.Probe),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(2,   Protoss.Dragoon),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(3,   Protoss.Dragoon),
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Dragoon),
    GetAtLeast(1,   Protoss.RoboticsFacility))
  
  val Opening2GateRoboPvT = Vector[BuildRequest] (
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(17,  Protoss.Probe),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(2,   Protoss.Dragoon),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Dragoon),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(1,   Protoss.RoboticsFacility))
  
  val Opening2GateObserver = Vector[BuildRequest] (
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),
    GetAtLeast(17,  Protoss.Probe),
    GetUpgrade(Protoss.DragoonRange),
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(2,   Protoss.Dragoon),
    GetAtLeast(22,  Protoss.Probe),
    GetAtLeast(1,   Protoss.RoboticsFacility),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Dragoon),
    GetAtLeast(24,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(25,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Pylon),
    GetAtLeast(4,   Protoss.Dragoon),
    GetAtLeast(26,  Protoss.Probe),
    GetAtLeast(5,   Protoss.Dragoon),
    GetAtLeast(27,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Observatory),
    GetAtLeast(6,   Protoss.Dragoon),
    GetAtLeast(28,  Protoss.Probe),
    GetAtLeast(5,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.Observer),
    GetAtLeast(29,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Dragoon),
    GetAtLeast(30,  Protoss.Probe))
  
  val OpeningDTExpand = Vector[BuildRequest] (
    // 2-Gate DT Expand based on PvP 2-Gate DT from http://wiki.teamliquid.net/starcraft/2_Gate_DT
    // We get gas/core faster because of mineral locking + later scout
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),             // 8
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),           // 10
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),       // 11
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),            // 13 -- this is important to deny scouting
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),             // 20 = 18 + Z
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),           // 21 = 19 + Z
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CitadelOfAdun),     // 24 = 20 + Z + D
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Dragoon),           // 25 = 21 + Z + D
    GetAtLeast(2,   Protoss.Gateway),           // 27 = 21 + Z + DD
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.TemplarArchives),
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Gateway),
    GetAtLeast(22,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Dragoon),
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Pylon),
    GetAtLeast(2,   Protoss.DarkTemplar),
    GetAtLeast(24,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus))
  
  //////////////////////
  // Protoss vs. Zerg //
  //////////////////////
  
  val FFE_Vs4Pool = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(2,   Protoss.PhotonCannon))
  
  val FFE_Conservative = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(9,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(2,   Protoss.PhotonCannon))
  
  val FFE_ForgeCannonNexus = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.PhotonCannon),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(3,   Protoss.PhotonCannon),
    GetAtLeast(2,   Protoss.Pylon))
  
  val FFE_NexusGatewayForge = Vector[BuildRequest] (
    GetAtLeast(1,   Protoss.Nexus),
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),
    GetAtLeast(12,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Nexus),
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),
    GetAtLeast(15,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Forge),
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),
    GetAtLeast(2,   Protoss.PhotonCannon))
}
