package Global.Resources.Scheduling

import scala.collection.mutable

class ScheduleSimulationEventQueue {
  val events:mutable.PriorityQueue[SimulationEvent] = new mutable.PriorityQueue
  
  def isEmpty:Boolean = {
    events.isEmpty
  }
  def head:SimulationEvent = {
    events.head
  }
  
  def enqueue(event:SimulationEvent) {
    events.enqueue(event)
  }
  
  def dequeue():SimulationEvent = {
    events.dequeue()
  }
}
