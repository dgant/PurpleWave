package Macro.Allocation

import Lifecycle.With
import Planning.Plan

import scala.collection.mutable

class Prioritizer {
  
  val priorities = new mutable.HashMap[Plan, Integer] {
    override def default(key: Plan): Integer = Integer.MAX_VALUE
  }
  
  var nextPriority = 0
  var lastRun = -1
  val frameDelays: mutable.Queue[Int] = new mutable.Queue[Int]
  
  def update() {
    nextPriority = 0
    frameDelays.enqueue(With.framesSince(lastRun))
    while(frameDelays.sum > 24 * 10) {
      frameDelays.dequeue()
    }
    lastRun = With.frame
    priorities.clear()
  }
  
  def audit = priorities.toList.sortBy(_._2)
  
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
