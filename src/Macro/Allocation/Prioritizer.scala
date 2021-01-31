package Macro.Allocation

import Lifecycle.With
import Planning.Prioritized

import scala.collection.mutable

class Prioritizer {
  
  private val priorities: mutable.HashMap[Prioritized, Integer] = new mutable.HashMap[Prioritized, Integer] {
    override def default(key: Prioritized): Integer = Integer.MAX_VALUE
  }
  
  var nextPriority: Int = 0
  var lastRun: Int = -1
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
  
  def all: List[(Prioritized, Integer)] = priorities.toList.sortBy(_._2)
  
  def isPrioritized(client: Prioritized): Boolean = {
    priorities.contains(client)
  }
  
  def getPriority(client: Prioritized): Integer = {
    priorities(client)
  }
  
  def prioritize(client: Prioritized) {
    if ( ! priorities.contains(client)) {
      priorities.put(client, nextPriority)
      nextPriority += 1
    }
  }
  
  override def toString: String = priorities.toVector.sortBy(_._2).map(_.toString).mkString("\n")
}
