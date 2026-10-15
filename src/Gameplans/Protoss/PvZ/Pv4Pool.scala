package Gameplans.Protoss.PvZ

import Lifecycle.With
import Macro.Actions.MacroActions
import ProxyBwapi.Races.Protoss
import Utilities.UnitFilters.IsWarrior

object Pv4Pool extends MacroActions {

  def apply(): Unit = {
    if (With.fingerprints.fourPool() && unitsEverComplete(IsWarrior) < 9) {
      once(8, Protoss.Probe)
      once(Protoss.Pylon)
      once(9, Protoss.Probe)
      if (have(Protoss.Forge)) {
        get(3, Protoss.PhotonCannon)
      }
      once(Protoss.Gateway)
      autosupply()
      pumpWorkers()
      pump(Protoss.Zealot)
      cancel(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Nexus)
      get(2, Protoss.Gateway)
    }
  }
}
