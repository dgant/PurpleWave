package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvRGFE

class PvRGFE extends GameplanImperative {

  override def activated: Boolean = PvRGFE()

  override def executeBuild():  Unit = {
    scoutOn(Protoss.Pylon)
    once(8,  Protoss.Probe)
    tryBuildInWall(1, Protoss.Pylon)
    once(9,  Protoss.Probe)
    tryBuildInWall(1, Protoss.Gateway)
    once(12,  Protoss.Probe)
  }

  override def executeMain(): Unit = {
    considerAttacking()
    pump(Protoss.Zealot)
    requireMiningBases(2)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
    // This should never happen
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
