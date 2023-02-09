package Planning.Plans.GamePlans.Protoss.PvR

import Planning.Plans.Compound._
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Utilities.UnitFilters.IsWarrior

class PvR2Gate4Gate extends GameplanImperative {

  override def executeBuild(): Unit = {
    buildOrder(ProtossBuilds.TwoGate910: _*)
  }

  override def executeMain(): Unit = {
    if (safePushing && (upgradeComplete(Protoss.DragoonRange) || unitsComplete(IsWarrior) >= 24)) attack()
    if (units(Protoss.Dragoon) >= 4) get(Protoss.DragoonRange)
    maintainMiningBases(1)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.Assimilator, Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(4, Protoss.Gateway)
    buildGasPumps()
  }
}
