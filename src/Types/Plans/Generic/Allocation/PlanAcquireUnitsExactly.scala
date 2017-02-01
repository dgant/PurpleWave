package Types.Plans.Generic.Allocation

import Startup.With
import UnitMatching.Matcher.UnitMatcher

class PlanAcquireUnitsExactly(
  val unitMatcher:UnitMatcher,
  val quantity: Integer = 0)
    extends PlanAcquireUnits {
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .filter(x => desiredUnits.size < quantity)
        .filter(unitMatcher.accept(_))
        .foreach(desiredUnits.add(_)))
    
    if (desiredUnits.size >= quantity) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
