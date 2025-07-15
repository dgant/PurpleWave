package Macro.Actions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Macro.Requests.RequestUnit
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.?

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

    lazy val builders = With.macroCounts.oursExtant(builderClass)
    lazy val hatches  = With.macroCounts.oursExtant(Zerg.Hatchery)
    lazy val capacity = ?(builderClass == Zerg.Larva, hatches * unitClass.copiesProduced, builders)
    val unitsComplete = With.macroCounts.oursComplete(unitClass)
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
