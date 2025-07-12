package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime
import Utilities.UnitFilters.IsHatchlike

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
    once(26, Zerg.Zergling) // 18 done as speed finishes, 8 following shortly thereafter
    get(Zerg.Lair, Zerg.Spire)
  }

  override def executeMain(): Unit = {
    var shouldAttack = true
    shouldAttack &&= Zerg.ZerglingSpeed() || ! enemyHasUpgrade(Zerg.ZerglingSpeed)
    shouldAttack &&= enemiesComplete(IsHatchlike) <= unitsComplete(IsHatchlike) || confidenceAttacking01 > 0.6
    shouldAttack &&= unitsComplete(Zerg.Mutalisk) >= enemies(Zerg.Mutalisk) || unitsComplete(Zerg.Zergling) > enemies(Zerg.Zergling)
    attack(shouldAttack)

    if (haveGasForUpgrade(Zerg.ZerglingSpeed) && ! haveEver(Zerg.Spire)) {
      gasWorkerCeiling(2)
    }
    once(8, Zerg.Mutalisk)
    upgradeContinuously(Zerg.ZerglingAttackSpeed)
    if (enemyProximity < 0.5) {
      get(11 * miningBases, Zerg.Drone)
      pumpGasPumps(units(Zerg.Drone) / 16)
      get(Zerg.QueensNest, Zerg.Hive)
    }
    pump(Zerg.Mutalisk)
    if ( ! enemiesHave(Zerg.Spire) && enemiesComplete(IsHatchlike) > unitsComplete(IsHatchlike)) {
      pump(Zerg.Drone, 11)
      buildSunkensAtFoyer(3, PlaceLabels.DefendHall)
    }
    pump(Zerg.Zergling)
    requireMiningBases(5)
    fillMacroHatches(10)
  }
}
