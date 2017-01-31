package Types.Requirements

import Startup.With

import scala.collection.mutable
  
abstract class RequireUnits extends Requirement {
  
  def units():mutable.Set[bwapi.Unit] = {
    With.recruiter.getUnits(this)
  }
  
  override def fulfill() {
    With.recruiter.fulfill(this)
  }
  
  override def abort() {
    With.recruiter.abort(this)
  }
  
  def offerBatchesOfUnits(candidates:Iterable[Iterable[bwapi.Unit]]):Iterable[bwapi.Unit]
}
