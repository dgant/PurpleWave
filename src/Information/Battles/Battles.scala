package Information.Battles

import Information.Battles.ProcessingStates.{BattleProcessInitial, BattleProcessState}
import Information.Battles.Types.{BattleGlobal, BattleLocal, Division}
import Lifecycle.With
import Performance.TaskQueue.TaskQueueGlobalWeights
import Performance.Tasks.{StateTasks, TimedTask}
import Performance.Timer
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Battles extends TimedTask {

  withAlwaysSafe(true)
  withSkipsMax(3)
  withWeight(TaskQueueGlobalWeights.Battles)

  var global    : BattleGlobal                = new BattleGlobal(Vector.empty, Vector.empty)
  var byUnit    : Map[UnitInfo, BattleLocal]  = Map.empty
  var local     : Vector[BattleLocal]         = Vector.empty
  var divisions : Vector[Division]            = Vector.empty

  var lastEstimationCompletion = 0
  val estimationRuntimes = new mutable.Queue[Int]

  var nextBattleGlobal  : Option[BattleGlobal]  = None
  var nextBattlesLocal  : Vector[BattleLocal]   = Vector.empty
  var nextDivisions     : Vector[Division]      = Vector.empty

  val clustering = new BattleClustering

  private var _processingState: BattleProcessState = new BattleProcessInitial
  def setProcessingState(newState: BattleProcessState): Unit = {
    _processingState = newState
  }

  val stateTasks = new StateTasks

  override def isComplete: Boolean = framesSinceRunning < 1 && _processingState.isFinalStep

  override def onRun(budgetMs: Long) {
    val timer = new Timer(budgetMs)
    while (timer.ongoing) {
      val task = stateTasks.get(_processingState)
      if (task.safeToRun(timer.remaining)) {
        task.runFunction = _processingState.step
        task.run(timer.remaining)
      } else {
        task.skip()
        return
      }
      if (_processingState.isFinalStep) {
        return
      }
    }
  }

  def measureReactionTime() {
    estimationRuntimes.enqueue(With.framesSince(lastEstimationCompletion))
    while (estimationRuntimes.sum > With.reaction.runtimeQueueDuration) { estimationRuntimes.dequeue() }
    lastEstimationCompletion = With.frame
  }
}
