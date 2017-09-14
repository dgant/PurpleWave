package Planning.Plans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestAnother, RequestAtLeast, RequestUpgrade}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
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
  
  val Opening_1GateZZCore = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe))
  
  val OpeningTwoGate99 = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe)
  )
  
  val OpeningTwoGate99_WithZealots = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(3,   Protoss.Zealot))
  
  val OpeningTwoGate910_WithZealots = Vector[BuildRequest] (
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
    RequestUpgrade(    Protoss.DragoonRange),
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
  
  val Opening13Nexus_Long = Vector[BuildRequest] (
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
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(23,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Pylon),
    RequestAtLeast(6,   Protoss.Dragoon),
    RequestAtLeast(25,  Protoss.Probe))
  
  val Opening1GateRangeExpand = Vector[BuildRequest] (
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
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(2,   Protoss.Gateway),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Dragoon),
    RequestAtLeast(22,  Protoss.Probe),
    RequestAtLeast(4,   Protoss.Dragoon))
  
  val OpeningDTExpand = Vector[BuildRequest] (
    // 2-Gate DT Expand based on PvP 2-Gate DT from http://wiki.teamliquid.net/starcraft/2_Gate_DT
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(20,  Protoss.Probe),
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
    RequestAtLeast(9,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(2,   Protoss.PhotonCannon),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.PhotonCannon),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(2,   Protoss.Pylon))
  
  val FFE_ForgeFirst = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(11,  Protoss.Probe), // Normally 12; 11 is protection against 4/5 pools
    RequestAtLeast(2,   Protoss.PhotonCannon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(3,   Protoss.PhotonCannon),
    RequestAtLeast(2,   Protoss.Pylon))
  
  val FFE_NexusFirst = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.PhotonCannon),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Zealot))
  
  val FFE_GatewayFirst_Aggressive = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    RequestAtLeast(18,  Protoss.Probe),
    RequestAnother(3,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator))
  
  /////////////////////
  // General-Purpose //
  /////////////////////
  
  val TechReavers = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(1,   Protoss.RoboticsFacility),
    RequestAtLeast(1,   Protoss.RoboticsSupportBay)
  )
}
