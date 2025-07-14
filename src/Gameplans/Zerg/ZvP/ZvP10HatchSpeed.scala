package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Zerg}
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
    once(20, Zerg.Zergling) // 18 done as speed finishes, 8 following shortly thereafter
    get(Zerg.Lair, Zerg.Spire)
  }

  override def executeMain(): Unit = {
    attack()

    if ( ! upgradeComplete(Zerg.ZerglingSpeed)) {
      aggression(0.5)
    }

    once(23, Zerg.Drone)
    if (With.fingerprints.twoGate()) {
      buildSunkensAtFoyer(2)
    } else {
      requireMiningBases(3)
    }

    if (haveGasForUpgrade(Zerg.ZerglingSpeed)) {
      if (haveEver(Zerg.Spire)) {
        pumpGasPumps(units(Zerg.Drone) / 9)
      } else {
        gasWorkerCeiling(2)

      }
    } else {
      gasWorkerCeiling(2)
    }

    once(10, Zerg.Mutalisk)

    if (haveComplete(Zerg.Hive)) {
      get(Zerg.GreaterSpire)
    }
    upgradeContinuously(Zerg.AirDamage) && upgradeContinuously(Zerg.AirArmor)
    upgradeContinuously(Zerg.ZerglingAttackSpeed)
    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundMeleeDamage)

    if (enemyProximity < 0.3) {
      pump(Zerg.Guardian, units(Zerg.Mutalisk) / 4)
    }
    pump(Zerg.Scourge, 2 * Math.min(enemies(Protoss.Corsair),  units(Zerg.Mutalisk)))
    pump(Zerg.Mutalisk)
    pump(Zerg.Zergling, 10)
    if (enemyProximity > 0.5) {
      pump(Zerg.Zergling)
    } else {
      pump(Zerg.Drone, Math.min(miningBases, 5) * 13)
    }

    get(Zerg.QueensNest, Zerg.Hive)
    if (miningBases >= 4 && have(Zerg.Hive)) {
      get(2, Zerg.EvolutionChamber)
    }
    get(Zerg.OverlordSpeed)

    pump(Zerg.Zergling)

    if (enemyProximity < 0.4) {
      requireMiningBases(7)
    } else {
      fillMacroHatches(21)
    }
  }
}
