package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.All.MacroActions
import Planning.Plans.Macro.Expanding.MaintainMiningBases
import Planning.Predicates.MacroCounting
import ProxyBwapi.Races.Zerg
import Utilities.UnitFilters.{IsTownHall, IsWorker}

class RequireEssentials extends Plan with MacroActions with MacroCounting {
  override def onUpdate(): Unit = {
    val haveWorkers     = units(IsWorker) > 0
    val haveHall        = units(IsTownHall) > 0
    val canMakeWorkers  = (haveHall || units(Zerg.Larva) > 0) && (minerals >= 50 || haveWorkers)
    val canMakeHall     = (haveWorkers || canMakeWorkers) && (minerals >= With.self.townHallClass.mineralPrice || (haveWorkers && haveHall))
    if (canMakeWorkers) {
      get(1, With.self.workerClass)
    }
    if (canMakeHall) {
      requireMiningBases(1)
    }
    if (canMakeWorkers) {
      get(3, With.self.workerClass)
      if (With.self.isZerg) {
        get(1, Zerg.Overlord)
      }
    }
    if (canMakeHall) {
      new MaintainMiningBases().update()
    }
  }
}
