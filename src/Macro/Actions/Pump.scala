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
      maximumTotal        : Int = 400,
      maximumConcurrently : Int = 400): Unit = {

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

    if (maximumConcurrently < 400) {
      var maxQuantity = 0
      (0 until wavesAhead).foreach(wave => {
        val quantity = Math.min(
          maximumTotal,
          unitsComplete + maximumConcurrently * (1 + wave))
        if (quantity > maxQuantity) {
          With.scheduler.request(
            this,
            RequestUnit(
              unitClass,
              quantity,
              ?(wave > 0, With.frame + unitClass.buildFrames * wave, 0)))
        }
        maxQuantity = Math.max(maxQuantity, quantity)
      })
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
