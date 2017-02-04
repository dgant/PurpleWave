package Plans.Generic.Allocation

import Traits.TraitSettableQuantity
import Startup.With

class PlanAcquireUnitsExactly
    extends PlanAcquireUnits
    with TraitSettableQuantity {
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    //The candidates are offered in pools.
    //Originally, we wanted to force plans to hire from the unemployed pool first
    //But that meant that when we had a strong preference, the pooling was overriding it
    //Flattening it basically retains the "welfare" effect, but still allows sorting to work
    candidates
      .flatten
      .toList
      .sortBy(getUnitPreference.preference(_))
      .foreach(unit =>
        if (desiredUnits.size < getQuantity
          && getUnitMatcher.accept(unit)) {
          desiredUnits.add(unit)
        })
    
    if (desiredUnits.size >= getQuantity) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
