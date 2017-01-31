package Types.Requirements

import Startup.With
import UnitMatching.Matcher.UnitMatcher

class RequireUnitsByQuantity(
  val quantity: Integer,
  val unitMatcher: UnitMatcher) extends RequireUnits {
  
  override def offerBatchesOfUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Iterable[bwapi.Unit] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .filter(x => desiredUnits.size < quantity)
        .filter(unitMatcher.accept(_))
        .foreach(desiredUnits.add(_)))
    
    isFulfilled = desiredUnits.size >= quantity
    
    desiredUnits
  }
}
