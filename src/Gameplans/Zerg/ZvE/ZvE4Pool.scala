package Gameplans.Zerg.ZvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.Zerg
import Utilities.UnitFilters.IsHatchlike

class ZvE4Pool extends GameplanImperative {

  override def executeBuild(): Unit = {
    get(Zerg.Overlord)
    get(Zerg.SpawningPool)
    once(5, Zerg.Drone)
    pump(Zerg.Zergling)

    var cancelExtractor = have(Zerg.Extractor)
    cancelExtractor ||= supplyUsed200 >= supplyTotal200
    cancelExtractor ||= supplyUsed200 < supplyTotal200 - 1
    cancelExtractor ||= With.units.ours.exists(u => Zerg.Extractor(u) && u.remainingCompletionFrames < 72)
    if (cancelExtractor) {
      cancel(Zerg.Extractor)
    } else if (supplyUsed200 == supplyTotal200 && have(Zerg.Larva) && minerals >= 100) {
      get(Zerg.Extractor)
    }
    if (supplyBlocked && With.units.ours.exists(u => IsHatchlike(u) && u.framesUntilLarva < 24)) {
      get(2, Zerg.Overlord)
    }
  }

  doAutosupply = false
  doBasics = false
  override def doWorkers(): Unit = {}


  var enableDroneWarfare: Boolean = false

  override def executeMain(): Unit = {
    attack()
    if (With.self.gatheredMinerals >= 382) {
      scout()
    }
    if (supplyUsed400 > supplyTotal400 && units(Zerg.Larva) >= 3) {
      aggression(1e10) // Not allin() because that screws up targeting logic
    }
    enableDroneWarfare ||= With.units.existsOurs(u => Zerg.Zergling(u) && (u.matchups.targetsInRange.nonEmpty || u.matchups.threatsInRange.nonEmpty))
    With.blackboard.droneWarfare.set(enableDroneWarfare)
  }
}
