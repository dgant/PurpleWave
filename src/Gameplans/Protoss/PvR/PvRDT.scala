package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Gameplans.Protoss.PvP.PvPIdeas
import Placement.Access.PlaceLabels.DefendHall
import ProxyBwapi.Races.Protoss
import Utilities.UnitFilters.IsWarrior

class PvRDT extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)
    once(12, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(Protoss.Assimilator)
    once(15, Protoss.Probe)
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(1, Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(Protoss.Dragoon)

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
    if (safePushing && haveEverComplete(Protoss.DarkTemplar)) {
      shouldAttack = true
    }
    if (shouldAttack) {
      status("Attack")
      attack()
    }

    maintainMiningBases(1)
    requireMiningBases(unitsComplete(IsWarrior) / 20)

    buildCannonsAtExpansions(2, DefendHall)
    pump(Protoss.DarkTemplar, 2)
    pump(Protoss.Dragoon)
    if (miningBases > 1) {
      upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor) && upgradeContinuously(Protoss.Shields)
    }

    get(Protoss.DragoonRange)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    get(2, Protoss.Gateway)
    buildCannonsAtFoyer(2)
    pumpGasPumps()
    get(4, Protoss.Gateway)
    requireMiningBases(2)
    get(4 * miningBases, Protoss.Gateway)
    requireMiningBases(3)
  }
}
