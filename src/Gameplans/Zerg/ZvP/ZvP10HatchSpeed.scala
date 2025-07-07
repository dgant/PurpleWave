package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

class ZvP10HatchSpeed extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(10, Zerg.Drone)
    if (With.frame < GameTime(1, 25)() && unitsEver(Zerg.Hatchery) == 1 && supplyTotal200 == 9) {
      if (haveEver(Zerg.Extractor)) {
        cancel(Zerg.Extractor)
      } else if (minerals >= 100) {
        get(Zerg.Extractor)
      }
    }
    requireMiningBases(2)
    get(Zerg.SpawningPool)
    once(13, Zerg.Drone) // I think an extra one is required here due to Extractor cancellation shenanigans. This should result in 9 +
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).requireBase(With.geography.ourMain))
    once(2, Zerg.Overlord)
    once(8, Zerg.Zergling)
    once(Zerg.ZerglingSpeed)
    once(12, Zerg.Zergling)
    once(3, Zerg.Overlord)
    once(26, Zerg.Zergling)
    get(Zerg.Lair, Zerg.Spire)
  }

  override def executeMain(): Unit = {
    attack()
    if (haveGasForUpgrade(Zerg.ZerglingSpeed) && ! haveEver(Zerg.Spire)) {
      gasWorkerCeiling(2)
    }
    once(8, Zerg.Mutalisk)
    if (enemyProximity < 0.5) {
      get(10 * miningBases, Zerg.Drone)
      pumpGasPumps(units(Zerg.Drone) / 16)
      get(Zerg.QueensNest, Zerg.Hive)
    }
    pump(Zerg.Mutalisk)
    get(Zerg.ZerglingAttackSpeed)
    pump(Zerg.Zergling)
    requireMiningBases(5)
    fillMacroHatches(10)
  }
}
