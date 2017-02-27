package Plans.Army.UnitAtLocation

import Plans.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Compound.AbstractPlanFulfillRequirements
import Startup.With
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferAnything, UnitPreference}
import Utilities.Property

class RequireUnitAtLocation extends AbstractPlanFulfillRequirements {
  
  val meRUAL = this
  val quantity        = new Property[Int]         (1)
  val range           = new Property[Int]         (32)
  val positionFinder  = new Property[PositionFinder]  (new PositionCenter)
  val unitMatcher     = new Property[UnitMatcher]     (UnitMatchAnything)
  val unitPreference  = new Property[UnitPreference]  (UnitPreferAnything)
  val unitPlan        = new Property[LockUnits]       (new LockUnitsExactly {
    this.quantity.inherit(meRUAL.quantity)
    this.unitMatcher.inherit(meRUAL.unitMatcher)
    this.unitPreference.inherit(meRUAL.unitPreference)
  })
  
  checker.set(new PlanCheckUnitAtLocation {
    this.description.set(Some("Check if a fighter is at the location"))
    this.quantity.inherit(meRUAL.quantity)
    this.range.inherit(meRUAL.range)
    this.positionFinder.inherit(meRUAL.positionFinder)
    this.unitMatcher.inherit(meRUAL.unitMatcher)
  })
  
  fulfiller.set(new PlanFulfillUnitAtLocation {
    this.description.set(Some("Send matching fighter(s) to the location"))
    this.unitPlan.inherit(meRUAL.unitPlan)
    this.positionFinder.inherit(meRUAL.positionFinder)
  })
  
  override def drawOverlay() = {
    if ( ! isComplete) {
      positionFinder.get.find.foreach(position => {
        With.game.drawCircleMap(
          position.toPosition,
          range.get,
          bwapi.Color.Green)
        With.game.drawTextMap(
          position.toPosition,
          "Requiring units")
      })
    }
  }
}
