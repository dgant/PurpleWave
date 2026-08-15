package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR2Gate1012

class PvR2Gate1012 extends GameplanImperative {

  override def activated: Boolean = PvR2Gate1012()

  override def executeBuild():  Unit = {
    scoutOn(Protoss.Pylon)
    once(8, Protoss.Probe)
    get(Protoss.Pylon)
    once(10, Protoss.Probe)
    get(1, Protoss.Gateway)
    once(12, Protoss.Probe)
    get(2, Protoss.Gateway)
    once(13, Protoss.Probe)
    once(1, Protoss.Zealot)
    once(14, Protoss.Probe)
    get(2, Protoss.Pylon)
    once(15,  Protoss.Probe)
    once(3,   Protoss.Zealot)
    once(16,  Protoss.Probe)
    get(3,   Protoss.Pylon)
    once(17,  Protoss.Probe)
    once(5,   Protoss.Zealot)
    once(18,  Protoss.Probe)
  }

  override def executeMain(): Unit = {
    considerAttacking()
    pump(Protoss.Zealot)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
  }
}
