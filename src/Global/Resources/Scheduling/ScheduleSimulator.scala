package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable

import scala.collection.mutable

object ScheduleSimulator {

  val maxDepthBuildables = 200
  val maxDepthFrames = 24 * 60 * 2
  
  def simulate:ScheduleSimulationResult = {
    val currentState = ScheduleSimulationStateBuilder.build
    var buildableQueue = With.scheduler.queue.toArray
    val eventsPlanned = new mutable.HashSet[SimulationEvent]
    val buildablesImpossible = new mutable.ListBuffer[Buildable]
  
    var index = 0
    while (index < buildableQueue.size && index < maxDepthBuildables) {
      
      val nextBuildable = buildableQueue(index)
      val build = currentState.tryBuilding(nextBuildable, maxDepthFrames + With.game.getFrameCount)
  
      if (build.buildEvent.isDefined) {
        val buildEvent = build.buildEvent.get
        eventsPlanned += buildEvent
        currentState.startEvent(buildEvent)
        index += 1
      }
      else if (build.unmetPrerequisites.nonEmpty) {
        buildableQueue = insertAt(buildableQueue, build.unmetPrerequisites, index)
      }
      else if ( ! build.exceededSearchDepth) {
        //We timed out, which is fine.
        index += 1
      }
      else {
        buildablesImpossible += nextBuildable
        index += 1
      }
    }
  
    new ScheduleSimulationResult(
      eventsPlanned,
      currentState.eventQueue,
      buildablesImpossible)
  }
  
  def insertAt(queue:Array[Buildable], insertions:Iterable[Buildable], index:Int):Array[Buildable] =
    queue.take(index) ++ insertions ++ queue.drop(index)
}
