package Planning.Plans.GamePlans.All

import Lifecycle.With
import Planning.Plans.GamePlans.GameplanImperative
import Planning.Plans.Placement.BuildCannonsAtExpansions
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanImperative {
  override def executeBuild(): Unit = {
    get(8, Protoss.Probe)
    get(1, Protoss.Pylon, With.geography.ourNatural)
    get(11, Protoss.Probe)
    get(Protoss.Forge)
    get(13, Protoss.Probe)
    get(2, Protoss.PhotonCannon, With.geography.ourNatural)
    get(2, Protoss.Nexus)
    get(15, Protoss.Probe)
    get(Protoss.Gateway)
    get(17, Protoss.Probe)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
  }
  override def executeMain(): Unit = {
    get(Protoss.DragoonRange)
    pump(Protoss.Observer, 2)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(2, Protoss.Gateway)
    get(Protoss.RoboticsFacility)
    buildGasPumps()
    get(Protoss.Observatory)
    new BuildCannonsAtExpansions(6)
    get(4, Protoss.Nexus)
    get(16, Protoss.Gateway)
  }
}
