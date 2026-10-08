package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import ProxyBwapi.Races.Protoss

class PvRGFEComplete extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    buildInWall(1, Protoss.Pylon)
    scoutOn(Protoss.Pylon)
    once(10, Protoss.Probe)
    buildInWall(1, Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(Protoss.Assimilator)
    once(15, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(20, Protoss.Probe)
    once(Protoss.DragoonRange)
    once(Protoss.Dragoon)
    once(21, Protoss.Probe)
  }

  override def executeMain(): Unit = {

  }
}
