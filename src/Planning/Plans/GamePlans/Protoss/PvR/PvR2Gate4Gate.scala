package Planning.Plans.GamePlans.Protoss.PvR

import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.PvP.PvPIdeas
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.UnitFilters.IsWarrior

class PvR2Gate4Gate extends GameplanImperative {

  override def executeBuild(): Unit = {
    buildOrder(ProtossBuilds.TwoGate1012: _*)
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
    buildGasPumps()
  }
}
