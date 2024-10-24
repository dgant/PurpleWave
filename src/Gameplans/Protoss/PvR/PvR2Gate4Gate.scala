package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Gameplans.Protoss.PvP.PvPIdeas
import ProxyBwapi.Races.{Protoss, Terran}
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
  }

  override def executeMain(): Unit = {
    var shouldAttack = false
    if (enemyDarkTemplarLikely) {
      shouldAttack = true
      PvPIdeas.requireTimelyDetection()
    }
    if (safePushing && (upgradeComplete(Protoss.DragoonRange) || unitsComplete(IsWarrior) >= 24)) {
      shouldAttack = true
    }
    if (shouldAttack) {
      status("Attack")
      attack()
    }
    if (enemyHasShown(Terran.Factory, Terran.Vulture)) {
      once(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Dragoon)
      get(Protoss.DragoonRange)
    }
    if (units(Protoss.Dragoon) >= 4) get(Protoss.DragoonRange)
    scoutOn(Protoss.Gateway, 2)
    maintainMiningBases(1)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.Assimilator, Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(4, Protoss.Gateway)
    pumpGasPumps()
  }
}
