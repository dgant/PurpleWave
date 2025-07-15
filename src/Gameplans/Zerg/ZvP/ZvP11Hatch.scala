package Gameplans.Zerg.ZvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.UnitFilters.{IsHatchlike, IsWarrior}
import Utilities.{?, SwapIf}

class ZvP11Hatch extends GameplanImperative {

  var scoutingEnabled : Boolean = false
  var openMutas       : Boolean = false


  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(11, Zerg.Drone)
    requireMiningBases(2)
    get(Zerg.SpawningPool)
    once(14, Zerg.Drone)
    once(?(With.fingerprints.forgeFe(), 2, 6), Zerg.Zergling)

    if ( ! haveEver(Zerg.HydraliskDen)) {
      openMutas ||= With.fingerprints.twoGate() || With.fingerprints.oneGateCore()
    }

    if (unitsEver(IsHatchlike) < 3) {
      if (openMutas) {
        fillMacroHatches(3)
      } else {
        requireMiningBases(3)
      }
    }
    get(Zerg.Extractor)

    scoutingEnabled ||= unitsComplete(Zerg.Drone) >= 10
    if (scoutingEnabled) {
      scout()
    }
  }

  override def executeMain(): Unit = {

    var shouldAttack = confidenceAttacking01 > 0.6
    shouldAttack &&= Zerg.HydraliskSpeed()
    shouldAttack ||= ! enemiesHave(Protoss.Dragoon) && ! enemyHasUpgrade(Protoss.ZealotSpeed)
    if (shouldAttack) {
      attack()
    }

    upgradeContinuously(Zerg.GroundRangeDamage) && upgradeContinuously(Zerg.GroundMeleeDamage)
    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.HydraliskSpeed)
    upgradeContinuously(Zerg.HydraliskRange)
    upgradeContinuously(Zerg.ZerglingAttackSpeed)
    if (upgradeStarted(Zerg.GroundRangeDamage, 2) && safeDefending) {
      get(Zerg.QueensNest, Zerg.Hive)
    }
    if (units(Zerg.Zergling) >= 12) {
      get(Zerg.ZerglingSpeed)
    }

    if (openMutas) {
      buildSunkensAtFoyer(2)
      once(10, Zerg.Zergling)
      once(26, Zerg.Drone)
      get(Zerg.Lair, Zerg.Spire)
      buildSunkensAtFoyer(5)
      get(2, Zerg.Extractor)
      get(Zerg.ZerglingSpeed)
      once(12, Zerg.Mutalisk)
      once(24, Zerg.Zergling)
      fillMacroHatches(5)
      get(Zerg.HydraliskDen)
    }

    get(Zerg.HydraliskDen, Zerg.Lair)
    get(Zerg.HydraliskSpeed)
    get(Zerg.Spire)
    once(2, Zerg.Scourge)
    once(9, Zerg.Hydralisk)

    pump(Zerg.Scourge, 2 + 2 * enemies(Protoss.Corsair, Protoss.Observer, Protoss.Shuttle, Protoss.Scout) + 6 * enemies(Protoss.Carrier))
    SwapIf(
      safeDefending || enemyProximity < 0.3,
      {
        if (openMutas && ! upgradeComplete(Zerg.HydraliskSpeed)) {
          pump(Zerg.Mutalisk)
        }
        pump(Zerg.Lurker, Maff.vmax(1, enemies(Protoss.Zealot) / 4, units(Zerg.Hydralisk) / 6))
        pump(Zerg.Hydralisk)
        pump(Zerg.Zergling)
      }, {
        once(9, Zerg.Hydralisk)
        pump(Zerg.Hydralisk, enemies(IsWarrior))
        pump(Zerg.Drone, Math.min(6, miningBases) * 12)
      })

    if (gas < 300) {
      pumpGasPumps(units(Zerg.Drone) / 9 + minerals / 500)
    }
    get(Zerg.EvolutionChamber)
    get(Zerg.LurkerMorph)
    requireMiningBases(4)
    fillMacroHatches(6)
    get(Zerg.OverlordSpeed)
    get(2, Zerg.EvolutionChamber)
    get(Zerg.QueensNest, Zerg.Hive)
    requireMiningBases(8)
    fillMacroHatches(24)
  }
}
