package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable

import scala.collection.mutable

object ScheduleSimulator {

  val maxDepthBuildables = 30
  val maxDepthFrames = 24 * 60 * 5
  
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
        currentState.eventQueue.add(buildEvent)
        //Kind of a hack: Reserve resources, but not units (because I haven't figured out how to reserve units yet)
        currentState.spendResources(buildEvent.buildable)
        index += 1
      }
      else if (build.unmetPrerequisites.nonEmpty) {
        buildableQueue = insertAt(buildableQueue, build.unmetPrerequisites, index)
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
