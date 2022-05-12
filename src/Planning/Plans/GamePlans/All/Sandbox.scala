package Planning.Plans.GamePlans.All

import Planning.Plans.GamePlans.GameplanImperative

class Sandbox extends GameplanImperative {
  override def executeBuild(): Unit = {
    /*
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
    */
  }
  override def executeMain(): Unit = {
    doBasics = false
    doBuildOrder = false
    /*
    scoutOn(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    pump(Protoss.Observer, 2)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(2, Protoss.Gateway)
    get(Protoss.RoboticsFacility)
    buildGasPumps()
    get(Protoss.Observatory)
    get(2, Protoss.Nexus)
    get(3, Protoss.Gateway)
    new BuildCannonsAtExpansions(6).update()
    get(15, Protoss.Nexus)
    get(Protoss.RoboticsSupportBay)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    get(Protoss.Stargate)
    get(Protoss.ArbiterTribunal)
    get(Protoss.FleetBeacon)
    get(2, Protoss.Stargate)
    get(32, Protoss.Gateway)
    */
  }
}
