package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.?
import Utilities.UnitFilters.{IsLairlike, IsWarrior}

class ZvPCrackling extends ZergGameplan {

  override def executeBuild(): Unit = {
    emergencyReactions()

    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    requireMiningBases(2)
    get(Zerg.SpawningPool)
    once(15, Zerg.Drone)
    requireMiningBases(3)
    once(8, Zerg.Zergling)
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).requireBase(With.geography.ourMain))
    once(3, Zerg.Overlord)
    get(Zerg.ZerglingSpeed)

    if ( ! foundEnemyBase) {
      scoutOn(Zerg.Hatchery, 3)
    }
  }

  override def executeMain(): Unit = {
    val airThreat = enemiesHaveComplete(Protoss.Stargate, Protoss.Corsair, Protoss.Scout)

    attack()
    once(18, Zerg.Zergling)

    upgradeContinuously(Zerg.ZerglingAttackSpeed)
    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundMeleeDamage)

    if (haveGasForUpgrade(Zerg.ZerglingSpeed)) {
      gasWorkerCeiling(units(Zerg.Drone) / 7)
    }
    if ( ! upgradeStarted(Zerg.ZerglingAttackSpeed) || airThreat) {
      get(Zerg.Lair)
    }
    if ( ! upgradeStarted(Zerg.ZerglingAttackSpeed)) {
      get(Zerg.QueensNest)
    }
    if (airThreat) {
      get(Zerg.Spire)
    }
    if ( ! upgradeStarted(Zerg.ZerglingAttackSpeed)) {
      get(Zerg.Hive)
    }

    if ( ! upgradeStarted(Zerg.ZerglingSpeed)) {
      gasLimitCeiling(200)
    } else if ( ! haveEver(IsLairlike)) {
      gasLimitCeiling(100)
    } else if ( ! haveEver(Zerg.QueensNest)) {
      gasLimitCeiling(?(airThreat, 250, 100))
    } else if ( ! haveEver(Zerg.Hive)) {
      gasLimitCeiling(?(airThreat, 350, 200))
    } else if (miningBases < 5) {
      gasLimitCeiling(?(airThreat, 400, 250))
    }

    get(Zerg.ZerglingAttackSpeed)
    pump(Zerg.Scourge, 2 * Maff.fromBoolean(enemiesShown(Protoss.Corsair) > 0) + 2 * enemies(Protoss.Shuttle, Protoss.Corsair, Protoss.Scout) + 8 * enemies(Protoss.Carrier))
    pump(Zerg.Zergling, 4 + (4 * enemies(IsWarrior) * (0.5 + 2 * enemyProximity)).toInt)
    pump(Zerg.Drone, Math.min(miningBases * 8, 30))
    pump(Zerg.Zergling)
    requireMiningBases(5)
    get(2, Zerg.EvolutionChamber)
    requireMiningBases(6)
    fillMacroHatches(12)
  }
}
