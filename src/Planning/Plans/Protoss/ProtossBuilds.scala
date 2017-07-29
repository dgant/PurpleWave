package Planning.Plans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  /////////////////////
  // General Purpose //
  /////////////////////
  
  val Opening_1GateCore = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
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
    RequestAtLeast(1,   Protoss.Dragoon)
  )
  
  val Opening_1GateZZCore = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe)
  )
  
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
  
  val OpeningTwoGate1015Dragoons = Vector[BuildRequest] (
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
    RequestUpgrade(    Protoss.DragoonRange),
    RequestAtLeast(2,   Protoss.Dragoon),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(4,   Protoss.Dragoon),
    RequestAtLeast(4,   Protoss.Pylon)
  )
  
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
  
  val OpeningDTExpand = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CitadelOfAdun),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),
    RequestAtLeast(20,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.TemplarArchives),
    RequestAtLeast(1,   Protoss.DarkTemplar),
    RequestAtLeast(2,   Protoss.Nexus)
  )
  
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
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.PhotonCannon),
    RequestAtLeast(16,  Protoss.Probe),
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
    RequestAtLeast(2,   Protoss.Pylon))
  
  val FFE_NexusFirst = Vector[BuildRequest] (
    RequestAtLeast(1,   Protoss.Nexus),
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),
    //This Gateway should only be placed vs 12-hatch, not 9-hatch
    //Our building placement also seems to demand a new Pylon for this
    //RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(1,   Protoss.Forge),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon))
  
  
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
