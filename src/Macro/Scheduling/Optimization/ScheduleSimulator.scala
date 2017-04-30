package Macro.Scheduling.Optimization

import Macro.Buildables.Buildable
import Macro.Scheduling.BuildEvent
import Lifecycle.With

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ScheduleSimulator {

  val maxDepthBuildables = 100
  val maxQueueOutput = 20
  val maxDepthFrames = 24 * 60 * 5
  
  def simulate(buildQueue:Iterable[Buildable]):ScheduleSimulationResult = {
    val currentState = ScheduleSimulationStateBuilder.build
    var buildablesRequested = new ArrayBuffer[Buildable] ++ buildQueue
    val buildablesImpossible = new mutable.HashSet[Buildable]
    val eventsPlanned = new mutable.PriorityQueue[BuildEvent]
    
    var index = 0
    while (index < buildablesRequested.size && index < maxDepthBuildables) {
      
      val nextBuildable = buildablesRequested(index)
      index += 1
      
      if (eventsPlanned.length >= maxQueueOutput ) {
        //Stop calculating here
      }
      else if (buildablesImpossible.contains(nextBuildable)) {
        //We already know we can't build this, so don't waste time trying :)
      }
      else
      {
        val build = currentState.tryBuilding(nextBuildable, maxDepthFrames + With.frame)
  
        if (build.buildEvent.isDefined) {
          val buildEvent = build.buildEvent.get
          eventsPlanned += buildEvent
          currentState.assumeEvent(buildEvent)
    
        }
        else if (build.unmetPrerequisites.nonEmpty) {
          index -= 1
          buildablesRequested.insertAll(index, build.unmetPrerequisites)
        }
        else if (build.exceededSearchDepth) {
          buildablesImpossible.add(nextBuildable)
        }
        else {
          buildablesImpossible.add(nextBuildable)
        }
      }
    }
  
    new ScheduleSimulationResult(
      eventsPlanned,
      currentState.eventQueue,
      buildablesImpossible)
  }
}
