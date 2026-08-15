package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvRFFE
import Utilities.?

class PvRFFE extends GameplanImperative {

  override def activated: Boolean = PvRFFE()

  override def executeBuild():  Unit = {
    scoutOn(Protoss.Pylon)
    scoutOn(Protoss.Forge, scouts = 2)
    once(8,  Protoss.Probe)
    tryBuildInWall(1, Protoss.Pylon)
    once(?(With.fingerprints.fourPool.recently, 10, 13), Protoss.Probe)
    tryBuildInWall(1, Protoss.Forge)
    once(14, Protoss.Probe)
    tryBuildInWall(2, Protoss.PhotonCannon)
    once(15, Protoss.Probe)
    requireMiningBases(2)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
    once(16, Protoss.Probe)
    tryBuildInWall(1, Protoss.Gateway)
    once(17, Protoss.Probe)
  }

  override def executeMain(): Unit = {
    // This should never happen
    considerAttacking()
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(5, Protoss.Gateway)
    if (gas < 300) {
      pumpGasPumps(2)
    }
    get(9, Protoss.Gateway)
  }
}
