package Planning.Plans.GamePlans.Protoss.PvZ

import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.Protoss
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvZ1BaseGoonReaver extends PvZ1BaseAllIn {

  override def executeMain(): Unit = {
    new MeldArchons()()
    pump(Protoss.Observer, 1)
    pumpShuttleAndReavers()

    SwapIf(
      unitsComplete(IsWarrior) >= 9, {
        // We will be gas-starved otherwise
        if (upgradeStarted(Protoss.DragoonRange)) {
          pump(Protoss.Dragoon)
        }
        pump(Protoss.Zealot)
      }, {
        get(
          Protoss.Gateway,
          Protoss.Assimilator,
          Protoss.CyberneticsCore,
          Protoss.Dragoon, // One, to deny scouting
          Protoss.RoboticsFacility,
          Protoss.Shuttle,
          Protoss.RoboticsSupportBay,
          Protoss.Reaver)
        get(Protoss.DragoonRange)
        get(3, Protoss.Gateway)
      })

    timingAttack ||= unitsComplete(Protoss.Reaver) >= 2 && unitsComplete(Protoss.Shuttle) >= 1 && Protoss.DragoonRange()
    needToAllIn     = mutalisksInBase
    allInLogic()
  }
}
