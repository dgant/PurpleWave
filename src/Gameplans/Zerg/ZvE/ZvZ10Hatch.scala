package Gameplans.Zerg.ZvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.Zerg
import Utilities.Time.Seconds
import Utilities.{?, SwapIf}

class ZvZ10Hatch extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    if (With.self.minerals >= 100) {
      get(Zerg.Extractor)
      once(10, Zerg.Drone)
      if (unitsEver(Zerg.Drone) >= 10 && ! haveEver(Zerg.SpawningPool)) {
        cancel(Zerg.Extractor)
      }
    }

    requireMiningBases(2)
    get(Zerg.SpawningPool)
    once(11, Zerg.Drone)
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).requireBase(With.geography.ourMain))
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    once(8, Zerg.Zergling)
    once(Zerg.ZerglingSpeed)
  }

  override def executeMain(): Unit = {
    if (safePushing && (Zerg.ZerglingSpeed() || enemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.tenHatch, With.fingerprints.twelvePool))) {
      attack()
    }
    harass()
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

    once(12, Zerg.Zergling)
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
        + 4 * Maff.fromBoolean(enemyMutalisksLikely)
        + 2 * units(Zerg.SporeColony)
        + 4 * enemyHydralisksLikely),
      Zerg.Drone)
    pumpGasPumps((units(Zerg.Drone) + 2) / 8)
    pump(Zerg.Zergling)
    get(Zerg.Lair)
    get(Zerg.Spire)
    requireMiningBases(3)
    get(9, Zerg.Hatchery, PlaceLabels.MacroHatch)
  }
}
