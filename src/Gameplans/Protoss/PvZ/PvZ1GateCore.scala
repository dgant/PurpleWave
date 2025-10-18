package Gameplans.Protoss.PvZ

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvZ1GateCore extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(9, Protoss.Probe)
    once(Protoss.Gateway)
    once(11, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(12, Protoss.Probe)
    once(Protoss.Zealot)
    once(13, Protoss.Probe)
    once(Protoss.Assimilator)
    once(14, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(15, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(16, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(17, Protoss.Probe)
    once(3, Protoss.Pylon)

    scoutOn(Protoss.Gateway)
  }

  override def executeMain(): Unit = {

    var shouldAttack: Boolean = false

    if (enemyLurkersLikely) {
      shouldAttack = true
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
    }

    if (enemyMutalisksLikely) {
      if ( ! enemiesHave(Zerg.Mutalisk)) {
        shouldAttack = true
      }
    } else {
      With.blackboard.acePilots.set(true)
    }

    if (safePushing && confidenceAttacking01 > 0.6 && upgradeComplete(Protoss.DragoonRange)) {
      shouldAttack = true
    }

    attack(shouldAttack)
    maintainMiningBases(1)
    if ( ! haveEver(Protoss.CyberneticsCore)) {
      gasWorkerCeiling(1)
    }
    gasLimitCeiling(400)

    if (enemyMutalisksLikely) {
      get(Protoss.Stargate)
      pump(Protoss.Corsair, 3 + 2 * enemies(Zerg.Mutalisk))
      get(Protoss.DragoonRange)
      if (enemyHasShown(Zerg.Spire, Zerg.Mutalisk, Zerg.Scourge)) {
        get(Protoss.AirArmor)
        get(Protoss.AirDamage)
      }
      pump(Protoss.Dragoon, 8 + 3 * enemies(Zerg.Mutalisk))
    }

    requireMiningBases(Math.min(4, unitsComplete(IsWarrior) / 18))

    SwapIf(
      safeDefending,
      {
        pump(Protoss.DarkTemplar, 1)
        if (upgradeStarted(Protoss.DragoonRange)) {
          if (upgradeStarted(Protoss.ZealotSpeed)) {
            pump(Protoss.Zealot, unitsComplete(Protoss.Dragoon) / 2)
          }
          pump(Protoss.Dragoon)
        }
        buildCannonsAtExpansions(2)
        pump(Protoss.Zealot)
      },
      if (have(Protoss.Forge)) {
        get(Protoss.CitadelOfAdun)
        get(Protoss.ZealotSpeed)
        get(Protoss.GroundDamage)
        upgradeContinuously(Protoss.GroundArmor) && upgradeContinuously(Protoss.GroundDamage)
      })


    get(Protoss.Stargate)
    once(2, Protoss.Corsair)
    get(Protoss.DragoonRange)
    get(3, Protoss.Gateway)

    requireBases(2)
    if (gas < 300) {
      pumpGasPumps()
    }
    get(Protoss.Forge)
    get(Protoss.TemplarArchives)

    get(5 * miningBases, Protoss.Gateway)
  }
}
