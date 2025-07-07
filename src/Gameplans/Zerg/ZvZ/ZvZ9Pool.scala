package Gameplans.Zerg.ZvZ

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.Zerg
import Utilities.UnitFilters.IsHatchlike

class ZvZ9Pool extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Zerg.Drone)
    get(Zerg.SpawningPool)
    once(10, Zerg.Drone)
    get(Zerg.Extractor)
    once(2, Zerg.Overlord)
    once(11, Zerg.Drone)
    once(6, Zerg.Zergling)
    get(Zerg.ZerglingSpeed)
    get(Zerg.Lair, Zerg.Spire)
  }

  override def executeMain(): Unit = {
    if (With.self.gatheredGas >= 200 && ! have(Zerg.Spire)) {
      gasLimitCeiling(150)
      gasWorkerCeiling(2)
    }
    once(12, Zerg.Zergling)
    once(6, Zerg.Mutalisk)
    if (enemyProximity < 0.5 && unitsComplete(Zerg.Spire) >  0) {
      pump(Zerg.Drone,
        Math.max(
          8 * miningBases,
          8
            + 2 * Maff.fromBoolean(enemyHasShown(Zerg.EvolutionChamber, Zerg.SporeColony))
            + 1 * Maff.fromBoolean(enemyHasShown(Zerg.Spire, Zerg.Mutalisk, Zerg.Scourge))
            + 3 * Maff.fromBoolean(enemyHasShown(Zerg.HydraliskDen, Zerg.Hydralisk))
            + 3 * With.units.countEverEnemy(Zerg.SporeColony)
            + 3 * With.units.countEverEnemy(Zerg.SunkenColony)
            + 6 * With.units.everEnemy.count(u => IsHatchlike(u) && ! u.base.filter(_.isMain).exists(_.townHall.contains(u)))))
    }
    if (units(Zerg.Mutalisk) >= 6 && enemyHasShown(Zerg.Spire, Zerg.Mutalisk, Zerg.Scourge)) {
      upgradeContinuously(Zerg.AirArmor, 2)
    }
    pump(Zerg.Mutalisk)
    pump(Zerg.Scourge, 2 * enemies(Zerg.Mutalisk))
    pump(Zerg.Zergling)
    pumpGasPumps((units(Zerg.Drone) + 5) / 8)
    if ( ! safeDefending && miningBases == 1 && ! haveComplete(Zerg.Spire) && enemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.twelvePool, With.fingerprints.tenHatch, With.fingerprints.twoHatchMain, With.fingerprints.fourPool)) {
      buildSunkensAtMain(1, PlaceLabels.DefendHall)
    }
    if (haveComplete(Zerg.Spire)) {
      requireMiningBases(3)
      fillMacroHatches(6)
    }

    if (safePushing && ( ! With.fingerprints.fourPool() || unitsComplete(Zerg.Mutalisk) >= 4)) {
      attack()
    }
    harass()
  }
}
