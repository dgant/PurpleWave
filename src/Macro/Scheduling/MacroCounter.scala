package Macro.Scheduling

import Lifecycle.With
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.CountMap

object MacroCounter {

  def countComplete(unit: FriendlyUnitInfo): CountMap[UnitClass] = {
    val output = new CountMap[UnitClass]

    if ( ! unit.alive) return output

    if (unit.complete) {
      output(unit.unitClass) = 1
    } else if (unit.completeOrNearlyComplete) {
      output(unit.buildType) = unit.buildType.copiesProduced
    }

    // Things that become an equivalent descended type immediately:
    // * Hive
    // * Lair
    // * Greater Spire
    //
    if (unit.is(Zerg.Hive)) {
      output(Zerg.Lair) = 1
      output(Zerg.Hatchery) = 1
    } else if (unit.is(Zerg.Lair)) {
      output(Zerg.Hatchery) = 1
    } else if (unit.is(Zerg.GreaterSpire)) {
      output(Zerg.Spire) = 1
    }

    // Count all tanks
    if (unit.is(UnitMatchSiegeTank)) {
      output(Terran.SiegeTankUnsieged) = 1
      output(Terran.SiegeTankSieged) = 1
    }

    output
  }

  def countCompleteOrIncomplete(unit: FriendlyUnitInfo): CountMap[UnitClass] = {
    val output = countComplete(unit)

    if ( ! unit.alive) return output

    output(unit.unitClass) = 1
    if (unit.buildType != UnitClasses.None) {
      output(unit.buildType) = unit.buildType.copiesProduced
    }

    output
  }

  def countFriendlyComplete: CountMap[UnitClass] = With.units.ours.map(countComplete).reduce(_ + _)
  def countFriendlyCompleteOrIncomplete: CountMap[UnitClass] = With.units.ours.map(countCompleteOrIncomplete).reduce(_ + _)
}
