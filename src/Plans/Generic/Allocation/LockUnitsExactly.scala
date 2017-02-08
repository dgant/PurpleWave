package Plans.Generic.Allocation

import Startup.With
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferAnything, UnitPreference}
import Traits.Property

class LockUnitsExactly extends LockUnits {
  
  description.set(Some("Reserve a fixed number of units"))
  
  val quantity        = new Property[Integer](1)
  val unitPreference  = new Property[UnitPreference](UnitPreferAnything)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    //The candidates are offered in pools.
    //Originally, we wanted to force plans to hire from the unemployed pool first
    //But that meant that when we had a strong preference, the pooling was overriding it
    //Flattening it basically retains the "welfare" effect, but still allows sorting to work
    candidates
      .flatten
      .toList
      .sortBy(unitPreference.get.preference(_))
      .foreach(unit =>
        if (desiredUnits.size < quantity.get
          && unitMatcher.get.accept(unit)) {
          desiredUnits.add(unit)
        })
    
    if (desiredUnits.size >= quantity.get) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
