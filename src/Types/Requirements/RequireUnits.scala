package Types.Requirements

import Startup.With
import Types.Contracts.{Buyer, PriorityMultiplier}
import UnitMatching.Matcher.UnitMatch
  
class RequireUnits (
  buyer: Buyer,
  priorityMultiplier: PriorityMultiplier,
  val unitMatcher:UnitMatch,
  val quantity:Integer)
    extends Requirement(
      buyer,
      priorityMultiplier) {
  
  def units():Set[bwapi.Unit] = {
    With.recruiter.getUnits(this)
  }
  
  override def fulfill() {
    With.recruiter.fulfill(this)
  }
  
  override def abort() {
    With.recruiter.abort(this)
  }
  
  def offerBatchesOfUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Iterable[bwapi.Unit] = {
    //It's important, for now, to start with our own units
    //Right now there's no way to unassign units that we decide not to use; they will be leaked
  }
}
