package Plans.Generic.Allocation

import Traits.TraitSettableQuantity
import Startup.With

class PlanAcquireUnitsExactly
    extends PlanAcquireUnits
    with TraitSettableQuantity {
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .toList
        .sortBy(getUnitPreference.preference(_))
        .foreach(unit =>
          if (desiredUnits.size < getQuantity
            && getUnitMatcher.accept(unit)) {
            desiredUnits.add(unit)
          }))
    
    if (desiredUnits.size >= getQuantity) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
