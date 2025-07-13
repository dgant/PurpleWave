package Gameplans.Zerg.ZvT

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Placement.Access.PlaceLabels.DefendHall
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.{?, SwapIf}
import Utilities.Time.Seconds

class ZvT3HatchCrazy extends ZergGameplan {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    requireMiningBases(2)
    once(13, Zerg.Drone)
    get(Zerg.SpawningPool)
    once(15, Zerg.Drone)
    requireMiningBases(3)
    once(16, Zerg.Drone)
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).preferBase(With.geography.ourMain))
    once(6, Zerg.Zergling)

    scoutOn(Zerg.Hatchery, 3)
  }

  override def executeMain(): Unit = {
    if ((safePushing && ! enemyHasShown(Terran.Vulture, Terran.Factory)) || haveComplete(Zerg.Mutalisk, Zerg.Ultralisk)) {
      attack()
    }

    gasLimitCeiling(1200)

    if (have(Zerg.Hive)) {
      get(Zerg.ZerglingSpeed)
    }
    get(Zerg.ZerglingAttackSpeed)
    SwapIf(
      With.fingerprints.bio(),
      get(Zerg.UltraliskSpeed),
      get(Zerg.UltraliskArmor))

    SwapIf(
      enemyProximity > 0.7,
      {
        get(Zerg.Lair, Zerg.Spire, Zerg.Hive, Zerg.UltraliskCavern)
        upgradeContinuously(Zerg.GroundArmor)
        upgradeContinuously(Zerg.GroundMeleeDamage)
        if (have(Zerg.Hive)) {
          get(2, Zerg.EvolutionChamber)
        }
        if (enemyHasTech(Terran.WraithCloak)) {
          get(Zerg.OverlordSpeed)
        }
      },
      {
        pump(Zerg.Mutalisk, 2 * enemies(Terran.Wraith) + enemies(Terran.Vulture) * (1 - unitsComplete(Zerg.UltraliskCavern)))
        pump(Zerg.Scourge, 2 * enemies(Terran.Wraith, Terran.Dropship) + 4 * enemies(Terran.Valkyrie) + 6 * enemies(Terran.Battlecruiser))
        pump(Zerg.Ultralisk)
        once(12, Zerg.Mutalisk)
        if (upgradeComplete(Zerg.ZerglingAttackSpeed, 1, Seconds(30)())) {
          pump(Zerg.Zergling)
        }
        if (enemyProximity > 0.5 && ! safeDefending) {
          pump(Zerg.Zergling, 16)
        }
        if (With.fingerprints.oneFac() && ! haveEver(Zerg.Mutalisk)) {
          buildSunkensAtOpenings(1)
        }
        if (With.fingerprints.bio() && ! haveEver(Zerg.Mutalisk) && ! safeDefending) {
          buildSunkensAtFoyer(?(With.fingerprints.twoRaxAcad(), 5, ?(With.fingerprints.twoRax1113() || With.fingerprints.bbs(), 3, 2)))
        }
        if (enemyProximity > 0.75) {
          pump(Zerg.SunkenColony)
        }
        if (enemyProximity < 0.4) {
          pump(Zerg.Drone, miningBases * 12)
        }
        if (enemyHasTech(Terran.WraithCloak) || enemies(Terran.Wraith) > 5) {
          buildSporesAtBases(1, DefendHall)
        }
        pump(Zerg.Mutalisk)
        pump(Zerg.Zergling, 4)
      })

    pump(Zerg.Drone, miningBases * 12)
    get(Zerg.EvolutionChamber, Zerg.QueensNest)

    pumpGasPumps(units(Zerg.Drone) / 2)
    requireMiningBases(4)
    get(2, Zerg.EvolutionChamber)
    requireMiningBases(7)
    pump(Zerg.Zergling)
  }
}
