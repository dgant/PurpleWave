package Plans.Information

import Plans.Allocation.{LockUnits, LockUnitsExactly}
import Plans.Compound.AbstractPlanFulfillRequirements
import Strategies.PositionFinders.{PositionCenter, PositionFinder}
import Strategies.UnitMatchers.{UnitMatchMobile, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Property

class RequireEnemyBaseLocation extends AbstractPlanFulfillRequirements {
  
  description.set(Some("We must know where an enemy's base is"))
  
  val meREBL = this
  val positionFinder = new Property[PositionFinder](new PositionCenter)
  val unitMatcher    = new Property[UnitMatcher]   (new UnitMatchMobile)
  val unitPreference = new Property[UnitPreference](new UnitPreferClose  { this.positionFinder.inherit(meREBL.positionFinder) })
  val scoutPlan      = new Property[LockUnits]     (new LockUnitsExactly {
    this.unitPreference.inherit(meREBL.unitPreference)
    this.unitMatcher.inherit(meREBL.unitMatcher)
  })
  
  checker.set(new KnowEnemyBaseLocationChecker)
  
  fulfiller.set(new KnowEnemyBaseLocationFulfiller {
    positionFinder.inherit(meREBL.positionFinder)
    unitPreference.inherit(meREBL.unitPreference)
    unitPlan.inherit(meREBL.scoutPlan)
  })
}
