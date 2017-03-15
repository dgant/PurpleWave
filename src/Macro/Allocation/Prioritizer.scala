package Macro.Allocation

import Planning.Plan
import Startup.With

import scala.collection.mutable

class Prioritizer {
  
  val _priorities = new mutable.HashMap[Plan, Integer]
  
  var _nextPriority = 0
  
  def onFrame() {
    _priorities.clear()
    _nextPriority = 0
    _prioritizeRecurisvely(With.gameplan)
  }
  
  def getPriority(plan:Plan):Integer = {
    _priorities.getOrElse(plan, Integer.MAX_VALUE)
  }
  
  def _prioritizeRecurisvely(plan:Plan) {
    if ( ! _priorities.contains(plan)) {
      _priorities.put(plan, _nextPriority)
      _nextPriority += 1
      plan.getChildren.foreach(_prioritizeRecurisvely)
    }
  }
}
