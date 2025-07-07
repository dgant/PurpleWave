package Gameplans.Zerg.ZvZ

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Zerg
import Utilities.Time.{GameTime, Minutes, Seconds}
import Utilities.{?, SwapIf}

class ZvZ10Hatch extends ZergGameplan {

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
    if (With.fingerprints.fourPool() && With.frame < Minutes(6)()) {
      cancel(Zerg.Hatchery)
      pump(Zerg.SunkenColony)
      pump(Zerg.Drone, 8)
      buildDefenseAtBases(2, Zerg.CreepColony, Seq(PlaceLabels.Defensive, PlaceLabels.DefendHall))
      if ( ! safeDefending && minerals < 100) {
        gasWorkerCeiling(0)
      }
      pump(Zerg.Mutalisk)
      pump(Zerg.Zergling)
      get(Zerg.Extractor, Zerg.Lair, Zerg.Spire)
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
    if (safePushing && (Zerg.ZerglingSpeed() || enemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool))) {
      attack()
    }
    harass()
    if (haveGasForUpgrade(Zerg.ZerglingSpeed)) {
      if ( ! have(Zerg.Spire)) {
        gasWorkerCeiling(?(have(Zerg.Lair), 2, units(Zerg.Drone) - 7))
      }
    } else {
      gasWorkerCeiling(2)
    }

    gasLimitCeiling(?(have(Zerg.Spire), minerals + 300, ?(have(Zerg.Lair), minerals + 200, 100)))

    val mutaliskArrival = With.scouting.earliestArrival(Zerg.Mutalisk)
    if (enemyMutalisksLikely
      && ! haveComplete(Zerg.Spire)
      && mutaliskArrival + Seconds(10)() < framesUntilUnit(Zerg.Mutalisk)) {
      get(Zerg.EvolutionChamber)
      buildDefenseAtBases(2, Zerg.CreepColony, Seq(PlaceLabels.Defensive, PlaceLabels.DefendHall, PlaceLabels.DefendAir))
    }
    pump(Zerg.SporeColony)
    pump(Zerg.Drone, 6)

    pump(Zerg.Scourge, Math.min(2 * units(Zerg.Mutalisk), 2 * enemies(Zerg.Mutalisk)))
    if (units(Zerg.Mutalisk) >= 6) {
      SwapIf(
        enemyHasShown(Zerg.Mutalisk),
        get(Zerg.AirDamage),
        get(Zerg.AirArmor))
    }

    pump(Zerg.Mutalisk)
    once(
      Math.min(
        10 * miningBases,
        12
        + 4 * enemies(Zerg.SunkenColony, Zerg.SporeColony)
        + 2 * units(Zerg.SporeColony)
        + 4 * Maff.fromBoolean(enemyMutalisksLikely)
        + 4 * Maff.fromBoolean(enemyHydralisksLikely)),
      Zerg.Drone)
    pumpGasPumps((units(Zerg.Drone) + 2) / 8)
    pump(Zerg.Zergling)
    requireMiningBases(3)
    get(9, Zerg.Hatchery, PlaceLabels.MacroHatch)
  }
}
