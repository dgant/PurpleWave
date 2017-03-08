package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable

import scala.collection.mutable.ListBuffer

object ScheduleSimulator {

  def simulate:Iterable[SimulationEvent] = {
    
    //Model the current macro state
    val state = ScheduleSimulationStateBuilder.build
    
    //Create a copy of the explicit build queue
    var queue = With.scheduler.queue.toArray
    
    //Start aggregating simulation events
    val output = new ListBuffer[SimulationEvent]
  
    //For each buildable in the queue
    var index = 0
    while (index < queue.size) {
      
      val next = queue(index)
  
      if (state.isBuildableEventually(next)) {
        // Enqueue it when it's first possible
        //
        output += state.startBuilding(next)
        index += 1
      }
      // Otherwise, the item has unmet requirements and will thus never be buildable
      //
      else {
        // Then insert the unmet requirements ahead of it in the queue
        // Prerequisites + additionalsupply
        //
        // TODO
  
        // Evaluate again at the same index
      }
    }
    
    output
  }
  
  def insertAt(queue:Array[Buildable], insertion:Buildable, index:Int):Array[Buildable] = {
    queue.take(index) ++ List(insertion) ++ queue.drop(index)
  }
}
