package Gameplans.Zerg.ZvP

import Gameplans.Zerg.ZvE.ZergGameplan
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.?
import Utilities.UnitFilters.{IsHatchlike, IsWarrior}

class ZvPHydraLurker extends ZergGameplan {

  var initialLings: Int = -1

  def isTwoGatey: Boolean = With.fingerprints.proxyGateway() || With.fingerprints.twoGate() || enemies(Protoss.Gateway) > 2 || enemiesShown(Protoss.Zealot) >= 3

  override def executeBuild(): Unit = {
    emergencyReactions()

    scoutAt(11)

    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(Zerg.SpawningPool)
    once(12, Zerg.Drone)
    requireMiningBases(2)

    initialLings =
        if (initialLings >= 0) {
          initialLings
        } else if (With.fingerprints.nexusFirst() || With.fingerprints.gatewayFe()) {
          6
        } else if (With.fingerprints.forgeFe()) {
          0
        } else if (isTwoGatey) {
          6
        } else if (haveComplete(Zerg.SpawningPool)) {
          4
        } else
          -1
    if (initialLings < 0) return

    once(initialLings, Zerg.Zergling)
    once(15 - initialLings / 2, Zerg.Drone)

    if (units(IsHatchlike) < 3) {
      if (With.fingerprints.proxyGateway()) {
        fillMacroHatches(1, With.geography.ourMain)
      } else if (isTwoGatey) {
        fillMacroHatches(1, With.geography.ourNatural)
      } else {
        requireMiningBases(3)
      }
    }
  }

  override def executeMain(): Unit = {
    if (initialLings < 0) return

    if (haveGasForUpgrade(Zerg.ZerglingSpeed) && ! upgradeComplete(Zerg.ZerglingSpeed)) {
      gasLimitCeiling(100)
    } else if ( ! techStarted(Zerg.LurkerMorph)) {
      gasLimitCeiling(250)
    }

    autosupply()

    if (confidenceAttacking01 > ?(upgradeComplete(Zerg.ZerglingSpeed), 0.55, ?(upgradeComplete(Zerg.HydraliskSpeed), 0.6, 0.65)) || ( ! enemyHasUpgrade(Protoss.ZealotSpeed) && ! enemyHasUpgrade(Protoss.DragoonRange))) {
      attack()
    }

    if ( ! haveComplete(Zerg.HydraliskDen)) {
      if (With.fingerprints.proxyGateway() || With.fingerprints.twoGate()) {
        get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).preferBase(With.geography.ourMain))
        get(Zerg.ZerglingSpeed)
        once(12, Zerg.Zergling)
      }
      pump(Zerg.Zergling, Maff.clamp((4 + enemies(IsWarrior) * 4 * enemyProximity).toInt, 4, 24))
      pump(Zerg.Drone)
    } else {
      get(Zerg.HydraliskSpeed)
      get(Zerg.HydraliskRange)
      val hydraliskTarget = Maff.clamp((5 + enemies(IsWarrior) * 3 * enemyProximity).toInt, 5, 24)
      pump(Zerg.Hydralisk, hydraliskTarget)
      pump(Zerg.Zergling, 3 * (hydraliskTarget - units(Zerg.Hydralisk)))
      pump(Zerg.Drone,  miningBases * 12)
    }

    get(Zerg.Extractor)
    get(Zerg.ZerglingSpeed)
    get(Zerg.HydraliskDen)
    requireMiningBases(3)
    fillMacroHatches(4)
    if (gas < 200) {
      pumpGasPumps(2)
    }

    get(Zerg.Lair)
    if (enemyDarkTemplarLikely) {
      get(Zerg.OverlordSpeed)
    }
    get(Zerg.EvolutionChamber)
    get(Zerg.LurkerMorph)
    get(Zerg.GroundArmor)
    pump(Zerg.Lurker, units(Zerg.Hydralisk) / 4)
    pumpGasPumps(3)
    requireMiningBases(4)

    upgradeContinuously(Zerg.GroundArmor)
    upgradeContinuously(Zerg.GroundRangeDamage)
    get(Zerg.QueensNest)
    get(Zerg.Hive)
    get(Zerg.ZerglingAttackSpeed)
    if (gas < 400) {
      pumpGasPumps()
    }
    pump(Zerg.Hydralisk)
    pump(Zerg.Zergling)
    fillMacroHatches(miningBases * 5)
  }
}
