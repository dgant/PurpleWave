package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Gameplans.Protoss.PvP.PvPIdeas
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvR2Gate4Gate extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Gateway)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(2, Protoss.Pylon)
    once(14, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(14, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(3, Protoss.Pylon)
    once(15, Protoss.Probe)
    once(5, Protoss.Zealot)
    once(17, Protoss.Probe)

    scoutOn(Protoss.Gateway, 2)
  }

  override def executeMain(): Unit = {
    var shouldAttack = false
    if (enemyDarkTemplarLikely) {
      shouldAttack = true
      PvPIdeas.requireTimelyDetection()
    } else if (enemyLurkersLikely || enemyShownCloakedThreat) {
      shouldAttack = true
      buildCannonsAtOpenings(1)
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      pump(Protoss.Observer, 2)
    }
    if (safePushing && (upgradeComplete(Protoss.DragoonRange) || unitsComplete(IsWarrior) >= 24)) {
      shouldAttack = true
    }
    if (shouldAttack) {
      status("Attack")
      attack()
    }
    if (enemyHasShown(Terran.Factory, Terran.Vulture)) {
      get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Dragoon)
      get(Protoss.DragoonRange)
    }
    if (units(Protoss.Dragoon) >= 2) {
      get(Protoss.DragoonRange)
    }
    if (units(Protoss.Zealot) >= 12) {
      get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.CitadelOfAdun)
      get(Protoss.ZealotSpeed)
    }

    maintainMiningBases(1)
    requireMiningBases(unitsComplete(IsWarrior) / 24)

    upgradeContinuously(Protoss.DragoonRange)
    upgradeContinuously(Protoss.GroundDamage)
    upgradeContinuously(Protoss.GroundArmor)
    upgradeContinuously(Protoss.ZealotSpeed)

    pump(Protoss.DarkTemplar, 1)
    pump(Protoss.Dragoon)
    SwapIf(
      enemyMutalisksLikely,
      {
        get(Protoss.Assimilator, Protoss.CyberneticsCore)
        get(Protoss.DragoonRange)
      },
      pump(Protoss.Zealot))

    get(4, Protoss.Gateway)
    requireMiningBases(2)

    get(2, Protoss.Forge)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)

    get(4 * miningBases, Protoss.Gateway)
    pumpGasPumps()

    requireMiningBases(3)
  }
}
