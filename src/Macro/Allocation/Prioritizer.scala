package Macro.Allocation

import Planning.Plan
import Startup.With

import scala.collection.mutable

class Prioritizer {
  
  private val priorities = new mutable.HashMap[Plan, Integer]
  
  def onFrame() {
    priorities.clear()
    prioritizeTree(With.gameplan)
  }
  
  def getPriority(plan:Plan):Integer = {
    priorities.getOrElse(plan, Integer.MAX_VALUE)
  }
  
  private def prioritizeTree(plan:Plan) {
    var nextPriority = 0
    if ( ! priorities.contains(plan)) {
      priorities.put(plan, nextPriority)
      nextPriority += 1
      plan.getChildren.foreach(prioritizeTree)
    }
  }
}
