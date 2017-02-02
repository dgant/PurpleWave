package Plans.Generic.Allocation

import Startup.With
import Types.UnitMatchers.UnitMatcher

class PlanAcquireUnitsExactly(
  val unitMatcher:UnitMatcher,
  val quantity: Integer = 0)
    extends PlanAcquireUnits {
  
  override def getRequiredUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Option[Iterable[bwapi.Unit]] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
      .foreach(pool => pool
        .foreach(unit =>
          if (desiredUnits.size < quantity && unitMatcher.accept(unit)) {
            desiredUnits.add(unit)
          }))
    
    if (desiredUnits.size >= quantity) {
      Some(desiredUnits)
    }
    else {
      None
    }
  }
}
