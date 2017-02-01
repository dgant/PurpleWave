package Types.Plans.Generic.Allocation

import Startup.With
import UnitMatchers.UnitMatcher

class PlanAcquireUnitsGreedily(
  val unitMatcher:UnitMatcher,
  val minimum: Integer = 0)
    extends PlanAcquireUnits {
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .filter(unitMatcher.accept(_))
        .foreach(desiredUnits.add(_)))
    
    if (desiredUnits.size >= minimum) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
