package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object PumpCount {

  def currentCount(unitClass: UnitClass): Int = {
    val matcher = UnitMatchOr(
      (unit: UnitInfo) => unit.is(Zerg.Egg) && unit.friendly.exists(_.buildType == unitClass),
      if (unitClass == Terran.SiegeTankSieged || unitClass == Terran.SiegeTankUnsieged) {
        UnitMatchSiegeTank
      }
      else if (unitClass == Zerg.Hatchery) {
        UnitMatchHatchery
      }
      else if (unitClass == Zerg.Lair) {
        UnitMatchLair
      }
      else if (unitClass == Zerg.Spire) {
        UnitMatchSpire
      }
      else unitClass)

    // Should this just be unit.alive?
    // Maybe this is compensating for a Scheduler
    var sum = 0
    With.units.ours.foreach(unit =>
      sum += (
        if (unit.complete && unit.alive && matcher.apply(unit)) {
          1
        }
        else if (unit.isAny(Zerg.Egg, Zerg.LurkerEgg, Zerg.Cocoon) && unit.buildType == unitClass) {
          unit.buildType.copiesProduced
        }
        else {
          0
        })
    )
    sum
  }
}
