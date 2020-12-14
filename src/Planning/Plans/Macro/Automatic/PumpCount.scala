package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

object PumpCount {

  def currentCount(unitClass: UnitClass): Int = {
    val matcher =
      if (unitClass == Terran.SiegeTankSieged || unitClass == Terran.SiegeTankUnsieged) {
        UnitMatchSiegeTank
      } else if (unitClass == Zerg.Hatchery) {
        UnitMatchHatchery
      } else if (unitClass == Zerg.Lair) {
        UnitMatchLair
      } else if (unitClass == Zerg.Spire) {
        UnitMatchSpire
      } else unitClass

    // Should this just be unit.alive?
    // Maybe this is compensating for a Scheduler
    var sum = 0
    With.units.ours.foreach(unit =>
      sum += (
        if (unit.alive && unit.complete && matcher(unit)) {
          1
        } else if (unit.buildType == unitClass && unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon)) {
          unit.buildType.copiesProduced
        } else 0)
    )
    sum
  }
}
