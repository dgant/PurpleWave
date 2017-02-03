package Plans.Generic.Compound

import Plans.Plan

abstract class AbstractPlanFulfillRequirements() extends Plan {
  
  def _getChecker():Plan
  def _getFulfiller():Plan
  
  override def children(): Iterable[Plan] = {
    List(_getChecker, _getFulfiller)
  }
  
  override def isComplete(): Boolean = {
    _getChecker.isComplete()
  }

  override def onFrame() {
    _getChecker.onFrame()
    if ( ! _getChecker.isComplete) {
      _getFulfiller.onFrame()
    }
  }
}
