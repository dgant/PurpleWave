package Macro.Actions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Macro.Requests.Get
import Mathematics.Maff
import Planning.Plans.NoPlan
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
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

    val unitsComplete       = With.macroCounts.oursComplete(unitClass)
    val larvaSpawning       = if (builderClass == Zerg.Larva) With.units.countOurs(IsAll(IsHatchlike, IsComplete)) else 0
    val builders            = getBuildersExisting + larvaSpawning
    val unitsToAdd          = Maff.vmin(maximumConcurrently, 2 * builders * unitClass.copiesProduced)
    val unitsToRequest      = Maff.vmin(maximumTotal, unitsComplete + unitsToAdd)
    With.scheduler.request(NoPlan(), Get(unitsToRequest, unitClass))
  }
}
