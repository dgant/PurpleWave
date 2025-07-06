package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.UnitFilters.IsWarrior

class ZvP3HatchBust extends ZergGameplan {

  override def executeBuild(): Unit = {
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
    attack()
    once(18, Zerg.Zergling)
    if (haveGasForUpgrade(Zerg.ZerglingSpeed)) {
      gasWorkerCeiling(units(Zerg.Drone) / 7)
    }
    get(Zerg.Lair, Zerg.Spire, Zerg.QueensNest, Zerg.Hive)
    get(Zerg.ZerglingAttackSpeed)
    pump(Zerg.Scourge, 2 + 3 * enemies(Protoss.Corsair, Protoss.Scout) + 8 * enemies(Protoss.Carrier))
    pump(Zerg.Zergling, 8 + (4 * enemies(IsWarrior) * (0.5 + enemyProximity)).toInt)
    pump(Zerg.Drone, miningBases * 5)
    pump(Zerg.Zergling)
    get(2, Zerg.EvolutionChamber)
    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundMeleeDamage)
    requireMiningBases(8)
  }
}
