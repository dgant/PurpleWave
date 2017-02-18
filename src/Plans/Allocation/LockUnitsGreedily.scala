package Plans.Allocation

import Startup.With
import Strategies.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferAnything, UnitPreference}
import Types.Property

import scala.collection.mutable

class LockUnitsGreedily extends LockUnits {
  
  description.set(Some("Reserve as many units as we can"))
  
  val unitMatcher = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference = new Property[UnitPreference](UnitPreferAnything)
  val minimum = new Property[Integer](1)
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
  
    val desiredUnits = With.recruiter.getUnits(this).to[mutable.Set]
    
    candidates
      .foreach(pool => pool
        .filter(unitMatcher.get.accept)
        .foreach(desiredUnits.add(_)))
    
    if (desiredUnits.size >= minimum.get) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
