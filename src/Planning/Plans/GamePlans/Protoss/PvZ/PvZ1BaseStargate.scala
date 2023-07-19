package Planning.Plans.GamePlans.Protoss.PvZ

import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.Protoss
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvZ1BaseStargate extends PvZ1BaseAllIn {

  override def executeMain(): Unit = {
    new MeldArchons()()
    pump(Protoss.Observer, 1)

    SwapIf(
      unitsComplete(IsWarrior) >= 9, {
        pump(Protoss.Scout)
        pump(Protoss.Zealot)
      }, {
        get(
          Protoss.Gateway,
          Protoss.Assimilator,
          Protoss.CyberneticsCore,
          Protoss.Stargate)
        get(2, Protoss.Gateway)
        get(2, Protoss.Stargate)
      })
    get(3, Protoss.Gateway)

    timingAttack  ||= unitsComplete(IsWarrior) >= 30
    needToAllIn     = mutalisksInBase || mutalisksImminent
    allInLogic()
  }
}
