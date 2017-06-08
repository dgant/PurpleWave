package Macro.Allocation

import Planning.Plan

import scala.collection.mutable

class Prioritizer {
  
  private val priorities = new mutable.HashMap[Plan, Integer] {
    override def default(key: Plan): Integer = Integer.MAX_VALUE
  }
  
  var nextPriority = 0
  
  def update() {
    //For debugging only!
    val sortedPriorities = priorities.toList.sortBy(_._2)
    nextPriority = 0
    priorities.clear()
  }
  
  def isPrioritized(plan: Plan): Boolean = {
    priorities.contains(plan)
  }
  
  def getPriority(plan: Plan): Integer = {
    priorities(plan)
  }
  
  def prioritize(plan: Plan) {
    if ( ! priorities.contains(plan)) {
      priorities.put(plan, nextPriority)
      nextPriority += 1
    }
  }
  
  override def toString: String = priorities.toVector.sortBy(_._2).map(_.toString).mkString("\n")
}
