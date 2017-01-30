package Types.Requirements

import Startup.With
import UnitMatching.Matcher.UnitMatch

import scala.collection.mutable
  
class RequireUnits (
  buyer: Buyer,
  priorityMultiplier: PriorityMultiplier,
  val unitMatcher:UnitMatch,
  val quantity:Integer)
    extends Requirement(
      buyer,
      priorityMultiplier) {
  
  def units():mutable.Set[bwapi.Unit] = {
    With.recruiter.getUnits(this)
  }
  
  override def fulfill() {
    With.recruiter.fulfill(this)
  }
  
  override def abort() {
    With.recruiter.abort(this)
  }
  
  def offerBatchesOfUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Iterable[bwapi.Unit] = {
    
    val desiredUnits = With.recruiter.getUnits(this).clone
    
    candidates
        .foreach(pool => pool
          .filter(x => (desiredUnits.size < quantity))
          .filter(unitMatcher.accept)
          .foreach(desiredUnits.add(_)))
    
    desiredUnits
  }
}
