package Planning.Plans.Gameplans.Protoss.PvZ

import ProxyBwapi.Races.Protoss
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvZ1Base4GateGoon extends PvZ1BaseAllIn {

  override def executeMain(): Unit = {
    pump(Protoss.Observer, 1)

    SwapIf(
      unitsComplete(IsWarrior) >= 9, {
        pump(Protoss.Dragoon)
        pump(Protoss.Zealot, 7)
      }, {
        get(
          Protoss.Gateway,
          Protoss.Assimilator,
          Protoss.CyberneticsCore,
          Protoss.Dragoon) // One, to deny scouting
        get(2, Protoss.Gateway)
        get(Protoss.DragoonRange)
        get(4, Protoss.Gateway)
      })
    pump(Protoss.Zealot)

    timingAttack  ||= Protoss.DragoonRange() && unitsComplete(Protoss.Dragoon) >= 12
    needToAllIn   ||= Protoss.DragoonRange() && mutalisksInBase
    allInLogic()
  }
}
