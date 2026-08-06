package Gameplans.All

import ProxyBwapi.Races.Zerg

class Sandbox extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    fillMacroHatches(2)
    once(13, Zerg.Drone)
    requireMiningBases(2)
    once(14, Zerg.Drone)
    fillMacroHatches(4)
    once(15, Zerg.Drone)
    requireMiningBases(3)
    once(16, Zerg.Drone)
    fillMacroHatches(6)
    once(17, Zerg.Drone)
    requireMiningBases(4)
  }

  override def executeMain(): Unit = {
  }
}
