package Macro.Actions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import ProxyBwapi.Races.Zerg
import Utilities.UnitFilters.{IsTownHall, IsWorker}

object RequireEssentials extends MacroActions {
  def apply(): Unit = {
    val haveWorkers     = MacroFacts.units(IsWorker) > 0
    val haveHall        = MacroFacts.units(IsTownHall) > 0
    val canMakeWorkers  = (haveHall || MacroFacts.units(Zerg.Larva) > 0) && (MacroFacts.minerals >= 50 || haveWorkers)
    val canMakeHall     = (haveWorkers || canMakeWorkers) && (MacroFacts.minerals >= With.self.townHallClass.mineralPrice || (haveWorkers && haveHall))
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
      if (With.units.ours.filter(_.unitClass.isResourceDepot).forall(_.complete)) {
        requireMiningBases(Maff.vmin(
          With.geography.maxMiningBasesOurs,
          MacroFacts.miningBases + 1,
          MacroFacts.units(IsWorker) * 15))
      }
    }
  }
}
