package Information.Battles

import Information.Battles.ProcessingStates.{BattleProcessInitial, BattleProcessState}
import Information.Battles.Types.{Battle, Division}
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

  var globalHome  : Battle                = new Battle(isGlobal = true)
  var globalAway  : Battle                = new Battle(isGlobal = true)
  var byUnit      : Map[UnitInfo, Battle] = Map.empty
  var local       : Vector[Battle]        = Vector.empty
  var divisions   : Vector[Division]      = Vector.empty

  var nextBattleGlobalHome  : Battle            = new Battle(isGlobal = true)
  var nextBattleGlobalAway  : Battle            = new Battle(isGlobal = true)
  var nextBattlesLocal      : Vector[Battle]    = Vector.empty
  var nextDivisions         : Vector[Division]  = Vector.empty
  def nextBattles: Seq[Battle] = nextBattlesLocal.view :+ nextBattleGlobalHome :+ nextBattleGlobalAway

  val clustering = new BattleClustering

  var lastEstimationCompletion = 0
  val estimationRuntimes = new mutable.Queue[Int]

  private var _processingState: BattleProcessState = new BattleProcessInitial
  def setProcessingState(newState: BattleProcessState): Unit = {
    _processingState = newState
  }

  val stateTasks = new StateTasks

  override def isComplete: Boolean = framesSinceRunning < 1 && _processingState.isFinalStep

  override def onRun(budgetMs: Long): Unit = {
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

  def measureReactionTime(): Unit = {
    estimationRuntimes.enqueue(With.framesSince(lastEstimationCompletion))
    while (estimationRuntimes.sum > With.reaction.runtimeQueueDuration) { estimationRuntimes.dequeue() }
    lastEstimationCompletion = With.frame
  }
}
