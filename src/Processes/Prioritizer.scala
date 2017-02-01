package Processes

import Startup.With
import Types.Plans.Plan

import scala.collection.mutable

class Prioritizer {
  var _nextPriority = 0
  
  val _priorities = new mutable.HashMap[Plan, Integer]
  
  def reassignPriorities() {
    _priorities.clear()
    _nextPriority = 0
    _prioritizeRecurisvely(With.gameplan)
  }
  
  def getPriority(plan:Plan):Integer = {
    _priorities.getOrElse(plan, Integer.MAX_VALUE)
  }
  
  def _prioritizeRecurisvely(plan:Plan) {
    _priorities.put(plan, _nextPriority)
    _nextPriority += 1
    plan._children.foreach(_prioritizeRecurisvely)
  }
}
