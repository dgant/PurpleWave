package Planning.Plans.GamePlans.All

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.Frames

class Sandbox extends GameplanImperative {

  var indexes: Set[Int] = Set.empty
  def logBench(index: Int, minerals: Int): Unit = {
    if (With.self.gatheredMinerals >= minerals && ! indexes.contains(index)) {
      indexes += index
      With.logger.debug(f"BENCHMARK: Hit threshold #$index at ${With.frame} frames ${Frames(With.frame)} + ${With.frame % 24} frames")
      if (indexes.size >= 5) { With.game.leaveGame() }
    }
  }

  override def executeBuild(): Unit = {
    if (With.frame == 0) {
      indexes = Set.empty
      With.logger.debug(f"BENCHMARK: Starting at ${With.geography.ourMain.townHallTile}")
    }
    With.configuration.enablePerformancePauses = false
    get(8, Protoss.Probe)
    get(Protoss.Pylon)
    get(10, Protoss.Probe)
    get(Protoss.Gateway)
    get(11, Protoss.Probe)
    get(Protoss.Assimilator)
    get(13, Protoss.Probe)
    get(Protoss.CyberneticsCore)
    get(14, Protoss.Probe)
    get(2, Protoss.Pylon)

    logBench(0, 300)
    logBench(1, 550)
    logBench(2, 700)
    logBench(3, 1000)
    logBench(4, 1150)

    /*
    logBench(0, units(Protoss.Pylon) > 0)
    logBench(1, units(Protoss.Gateway) > 0)
    logBench(2, units(Protoss.Assimilator) > 0)
    logBench(3, units(Protoss.CyberneticsCore) > 0)
    logBench(4, units(Protoss.Pylon) > 1)
*/



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
    doBasics = true
    doBuildOrder = true
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
