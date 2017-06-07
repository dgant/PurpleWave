package Macro.Allocation

import Planning.Plan

import scala.collection.mutable

class Prioritizer {
  
  private val priorities = new mutable.HashMap[Plan, Integer]
  var nextPriority = 0
  
  def active(plan:Plan): Boolean = {
    priorities.contains(plan)
  }
  
  def update() {
    //For debugging only!
    val sortedPriorities = priorities.toList.sortBy(_._2)
    nextPriority = 0
    priorities.clear()
    // prioritizeTree(With.gameplan)
  }
  
  def getPriority(plan:Plan):Integer = {
    prioritize(plan)
    priorities(plan)
  }
  
  def prioritize(plan:Plan) {
    if ( ! priorities.contains(plan)) {
      priorities.put(plan, nextPriority)
      nextPriority += 1
    }
  }
  
  private def prioritizeTree(plan:Plan) {
    prioritize(plan)
    plan.getChildren.foreach(prioritizeTree)
  }
  
  override def toString: String = priorities.toVector.sortBy(_._2).map(_.toString).mkString("\n")
}
