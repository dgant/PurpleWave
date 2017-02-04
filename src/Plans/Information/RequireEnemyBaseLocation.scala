package Plans.Information

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Generic.Compound.AbstractPlanFulfillRequirements
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Traits.Property

class RequireEnemyBaseLocation extends AbstractPlanFulfillRequirements {
  
  val me = this
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose  { positionFinder.inherit(me.positionFinder) })
  val unitPlan       = new Property[LockUnits]     (new LockUnitsExactly { unitPreference.inherit(me.unitPreference) })
  
  checker.set(new PlanCheckKnowingEnemyBaseLocation {
    positionFinder.inherit(me.positionFinder)
    unitPreference.inherit(me.unitPreference)
    unitPlan.inherit(me.unitPlan)
  })
  
  fulfiller.set(new PlanFulfillKnowingEnemyBaseLocation)
}
