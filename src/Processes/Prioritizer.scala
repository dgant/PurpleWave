package Processes

import Startup.With
import Types.Plans.Plan
import Types.Traits.ResourceRequest

class Prioritizer {
  var _nextPriority:Integer = 0
  
  def reassignPriorities() {
    _nextPriority = 0
    _prioritizeRecurisvely(With.gameplan)
  }
  
  def _prioritizeRecurisvely(plan:Plan) {
    Iterable(plan)
      .filter(_.isInstanceOf[ResourceRequest])
      .map(_.asInstanceOf[ResourceRequest])
      .foreach(plan => {
        plan.priority = _nextPriority
        _nextPriority += 1
      })
    
    plan._children.foreach(_prioritizeRecurisvely)
  }
}
