package Plans.Information

import Plans.Generic.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Generic.Compound.AbstractPlanFulfillRequirements
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Traits.Property

class RequireEnemyBaseLocation extends AbstractPlanFulfillRequirements {
  
  description.set(Some("We must know where an enemy's base is"))
  
  val meREBL = this
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose  { positionFinder.inherit(meREBL.positionFinder) })
  val scoutPlan      = new Property[LockUnits]     (new LockUnitsExactly { unitPreference.inherit(meREBL.unitPreference) })
  
  checker.set(new KnowEnemyBaseLocationChecker)
  
  fulfiller.set(new KnowEnemyBaseLocationFulfiller {
    positionFinder.inherit(meREBL.positionFinder)
    unitPreference.inherit(meREBL.unitPreference)
    unitPlan.inherit(meREBL.scoutPlan)
  })
}
