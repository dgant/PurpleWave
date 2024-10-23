package Gameplans.Protoss.PvZ

import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.UnitFilters.IsWarrior
import Utilities.{?, SwapIf}

class PvZ1BaseStargate extends PvZ1BaseAllIn {

  override def executeMain(): Unit = {
    makeArchons()
    pump(Protoss.Observer, 1)

    SwapIf(
      unitsComplete(IsWarrior) >= 9, {
        if (units(Protoss.Corsair, Protoss.Scout) >= 2) {
          pump(Protoss.Dragoon, ?(enemiesHave(Zerg.Scourge), 2, 1))
        }
        if ( ! enemyHydralisksLikely) {
          pump(Protoss.Corsair, ?(enemyMutalisksLikely, Maff.clamp(enemies(Zerg.Mutalisk) + 2, 5, 16), 3))
        }
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
    needToAllIn     = mutalisksInBase
    allInLogic()
    if (mutalisksInBase) {
      aggression(10.0) // YOLO makes our air units dive into spores
      attack()
    }
  }
}
