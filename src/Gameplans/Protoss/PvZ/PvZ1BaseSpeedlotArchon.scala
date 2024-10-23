package Gameplans.Protoss.PvZ

import ProxyBwapi.Races.Protoss
import Utilities.SwapIf
import Utilities.UnitFilters.IsWarrior

class PvZ1BaseSpeedlotArchon extends PvZ1BaseAllIn {

  override def executeMain(): Unit = {
    makeArchons()
    pump(Protoss.Observer, 1)

    SwapIf(
      unitsComplete(IsWarrior) >= 9, {
        pump(Protoss.HighTemplar)
        pump(Protoss.Zealot)
      }, {
        get(
          Protoss.Gateway,
          Protoss.Assimilator,
          Protoss.CyberneticsCore,
          Protoss.Dragoon, // One, to deny scouting
          Protoss.Forge,
          Protoss.CitadelOfAdun)
        get(Protoss.GroundDamage)
        get(Protoss.ZealotSpeed)
        get(Protoss.TemplarArchives)
        get(4, Protoss.Gateway)
      })
    get(5, Protoss.Gateway)

    timingAttack  ||= Protoss.GroundDamage() && Protoss.ZealotSpeed() && units(Protoss.Archon) >= 2
    needToAllIn     = mutalisksInBase || mutalisksImminent
    allInLogic()
  }
}
