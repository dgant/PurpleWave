package Macro.Actions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Macro.Requests.RequestUnit
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?
import Utilities.UnitFilters._

object Pump {
  def apply(
      unitClass           : UnitClass,
      maximumTotal        : Int = Int.MaxValue,
      maximumConcurrently : Int = Int.MaxValue): Unit = {

    val builderClass: UnitClass = unitClass.whatBuilds._1

    val canBuild: Boolean = (
      unitClass.buildTechEnabling.forall(MacroFacts.techStarted)
        && unitClass.buildUnitsEnabling.forall(With.units.existsOurs(_))
        && unitClass.buildUnitsBorrowed.forall(With.units.existsOurs(_)))

    if ( ! canBuild) return

    val getBuildersExisting: Int = With.units.ours.count(builder =>
      builderClass(builder)
        && ( unitClass != Terran.NuclearMissile                           || ! builder.hasNuke)
        && ( ! unitClass.requiresPsi                                      || builder.powered)
        && ( ! unitClass.isAddon                                          || builder.addon.isEmpty)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.MachineShop)  || builder.addon.isDefined)
        && ( ! unitClass.buildUnitsEnabling.contains(Terran.ControlTower) || builder.addon.isDefined))

    val unitsComplete = With.macroCounts.oursComplete(unitClass)
    val larvaSpawning = ?(builderClass == Zerg.Larva, With.units.countOurs(IsAll(IsHatchlike, IsComplete)), 0)
    val capacity      = (getBuildersExisting + larvaSpawning) * unitClass.copiesProduced
    val wavesAhead    = 4

    if (maximumConcurrently < Int.MaxValue / wavesAhead) {
      (0 until wavesAhead).foreach(wave =>
        With.scheduler.request(
          this,
          RequestUnit(
            unitClass,
            Math.min(
              maximumTotal,
              unitsComplete + capacity * (1 + wave)),
            unitClass.buildFrames * wave)))
    } else {
      With.scheduler.request(
        this,
        RequestUnit(
          unitClass,
          Math.min(
            maximumTotal,
            unitsComplete + capacity * wavesAhead)))
    }
  }
}
