package Types.Requirements

import Startup.With
import UnitMatching.Matcher.UnitMatcher

class RequireUnitsGreedy(
  val minimum: Integer,
  val unitMatcher:UnitMatcher) extends RequireUnits {
  
  override def offerBatchesOfUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Iterable[bwapi.Unit] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .filter(unitMatcher.accept(_))
        .foreach(desiredUnits.add(_)))
    
    isFulfilled = desiredUnits.size >= minimum
    
    desiredUnits
  }
}
