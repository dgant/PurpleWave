package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestUnitAtLeast, RequestUpgrade}
import ProxyBwapi.Races.Protoss

object ProtossBuilds {
  
  val OpeningOneGateCore = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.Nexus),
    RequestUnitAtLeast(8,   Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Pylon),
    RequestUnitAtLeast(10,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Gateway),
    RequestUnitAtLeast(12,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Assimilator),
    RequestUnitAtLeast(13,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    RequestUnitAtLeast(15,  Protoss.Probe),
    RequestUnitAtLeast(2,   Protoss.Pylon),
    RequestUnitAtLeast(1,   Protoss.Dragoon)
  )
  
  val OpeningTwoGate99 = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Nexus),
    RequestUnitAtLeast(9, Protoss.Probe),
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(2, Protoss.Gateway),
    RequestUnitAtLeast(11, Protoss.Probe)
  )
  
  val OpeningTwoGate1012 = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Nexus),
    RequestUnitAtLeast(8, Protoss.Probe),
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(10, Protoss.Probe),
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(12, Protoss.Probe),
    RequestUnitAtLeast(2, Protoss.Gateway),
    RequestUnitAtLeast(13, Protoss.Probe)
  )
  
  val OpeningTwoGate99Zealots = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Zealot),
    RequestUnitAtLeast(2, Protoss.Pylon),
    RequestUnitAtLeast(2, Protoss.Zealot)
  )
  
  val OpeningTwoGate1012Zealots = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Zealot),
    RequestUnitAtLeast(14, Protoss.Probe),
    RequestUnitAtLeast(2, Protoss.Pylon),
    RequestUnitAtLeast(3, Protoss.Zealot)
  )
  
  val OpeningTwoGate1015 = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.Nexus),
    RequestUnitAtLeast(8,   Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Pylon),
    RequestUnitAtLeast(10,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Gateway),
    RequestUnitAtLeast(11,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.Assimilator),
    RequestUnitAtLeast(13,  Protoss.Probe),
    RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    RequestUnitAtLeast(15,  Protoss.Probe),
    RequestUnitAtLeast(2,   Protoss.Gateway),
    RequestUnitAtLeast(2,   Protoss.Pylon),
    RequestUpgrade(         Protoss.DragoonRange),
    RequestUnitAtLeast(2,   Protoss.Dragoon),
    RequestUnitAtLeast(3,   Protoss.Pylon),
    RequestUnitAtLeast(6,   Protoss.Dragoon)
  )
  
  val Opening12Nexus = Vector[BuildRequest] (
    new RequestUnitAtLeast(1,   Protoss.Nexus),
    new RequestUnitAtLeast(8,   Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Pylon),
    new RequestUnitAtLeast(12,  Protoss.Probe),
    new RequestUnitAtLeast(2,   Protoss.Nexus),
    new RequestUnitAtLeast(14,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Gateway),
    new RequestUnitAtLeast(15,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.Assimilator),
    new RequestUnitAtLeast(17,  Protoss.Probe),
    new RequestUnitAtLeast(1,   Protoss.CyberneticsCore),
    new RequestUnitAtLeast(2,   Protoss.Gateway),
    new RequestUnitAtLeast(1,   Protoss.Zealot),
    new RequestUnitAtLeast(19,  Protoss.Probe),
    new RequestUnitAtLeast(2,   Protoss.Pylon),
    new RequestUnitAtLeast(2,   Protoss.Dragoon),
    new RequestUpgrade(         Protoss.DragoonRange),
    new RequestUnitAtLeast(21,  Protoss.Probe),
    new RequestUnitAtLeast(3,   Protoss.Pylon),
    new RequestUnitAtLeast(4,   Protoss.Dragoon),
    new RequestUnitAtLeast(23,  Protoss.Probe),
    new RequestUnitAtLeast(3,   Protoss.Pylon),
    new RequestUnitAtLeast(6,   Protoss.Dragoon)
  )
  
  val TechDragoons = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.Assimilator),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUpgrade(Protoss.DragoonRange)
  )
  
  val TechReavers = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.Assimilator),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    RequestUnitAtLeast(1, Protoss.RoboticsSupportBay)
  )
  
  val TechCorsairs = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.Assimilator),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUnitAtLeast(1, Protoss.Stargate)
  )
  
  val TechDarkTemplar = Vector[BuildRequest] (
    RequestUnitAtLeast(1, Protoss.Pylon),
    RequestUnitAtLeast(1, Protoss.Gateway),
    RequestUnitAtLeast(1, Protoss.Assimilator),
    RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    RequestUnitAtLeast(1, Protoss.CitadelOfAdun),
    RequestUnitAtLeast(1, Protoss.TemplarArchives)
  )
  
  val TakeNatural = Vector[BuildRequest] (
    RequestUnitAtLeast(2, Protoss.Nexus)
  )
}
