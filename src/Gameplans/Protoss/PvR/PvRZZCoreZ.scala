package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvRZZCoreZ

class PvRZZCoreZ extends GameplanImperative {

  override def activated: Boolean = PvRZZCoreZ()

  override def executeBuild():  Unit = {
    scoutOn(Protoss.Pylon)
    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    scoutOn(Protoss.Gateway)
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
    once(2, Protoss.Nexus)
  }

  override def executeMain(): Unit = {
    considerAttacking()
    pump(Protoss.Zealot)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
  }
}
