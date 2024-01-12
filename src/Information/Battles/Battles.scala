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

  var byUnit                    : Map[UnitInfo, Battle] = Map.empty
  var local                     : Vector[Battle]        = Vector.empty
  var divisions                 : Vector[Division]      = Vector.empty
  var globalDefend              : Battle                = new Battle(isGlobal = true)
  var globalAttack              : Battle                = new Battle(isGlobal = true)
  var globalSlug                : Battle                = new Battle(isGlobal = true)
  var globalSkirmish            : Battle                = new Battle(isGlobal = true)
  var nextBattleGlobalDefend    : Battle                = new Battle(isGlobal = true)
  var nextBattleGlobalAttack    : Battle                = new Battle(isGlobal = true)
  var nextBattleGlobalSlug      : Battle                = new Battle(isGlobal = true)
  var nextBattleGlobalSkirmish  : Battle                = new Battle(isGlobal = true)
  var nextBattlesLocal          : Vector[Battle]        = Vector.empty
  var nextDivisions             : Vector[Division]      = Vector.empty
  var lastEstimationCompletion  : Int                   = 0

  val stateTasks          = new StateTasks
  val clustering          = new BattleClustering
  val estimationRuntimes  = new mutable.Queue[Int]

  def nextBattles: Seq[Battle] = nextBattlesLocal ++ Seq(
    nextBattleGlobalDefend,
    nextBattleGlobalAttack,
    nextBattleGlobalSlug,
    nextBattleGlobalSkirmish)

  private var _processingState: BattleProcessState = new BattleProcessInitial
  def setProcessingState(newState: BattleProcessState): Unit = {
    _processingState = newState
  }

  override def isComplete: Boolean = framesSinceRunning < 1 && _processingState.isFinalStep

  override def onRun(budgetMs: Long): Unit = {
    val timer = new Timer(budgetMs)
    while (timer.greenLight && With.simulation.future.forall(_.isCompleted)) {
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
