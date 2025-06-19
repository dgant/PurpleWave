package Gameplans.Zerg.ZvP

import Gameplans.All.GameplanImperative
import ProxyBwapi.Races.Zerg

class ZergVsProtoss extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    requireMiningBases(2)
    once(13, Zerg.Drone)
    requireMiningBases(3)
    once(Zerg.SpawningPool)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(maximumTotal = 9 * unitsComplete(Zerg.Hatchery))
  }

  def executeMain(): Unit = {
    requireMiningBases(5)
    buildSunkensAtOpenings(8)
    pump(Zerg.Zergling)
    attack()
  }
}