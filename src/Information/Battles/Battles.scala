package Information.Battles

import Information.Battles.Clustering.BattleClustering
import Information.Battles.ProcessingStates.{BattleProcessInitial, BattleProcessState}
import Information.Battles.Types.{BattleGlobal, BattleLocal, Division, Team}
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Battles {
  var global    : BattleGlobal                = new BattleGlobal(new Team(Vector.empty), new Team(Vector.empty))
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

  def run() {
    local.foreach(_.updateFoci())
    var proceed = true
    while (proceed && With.performance.continueRunning) {
      proceed = ! _processingState.isFinalStep
      _processingState.step()
    }
  }

  def measureReactionTime() {
    estimationRuntimes.enqueue(With.framesSince(lastEstimationCompletion))
    while (estimationRuntimes.sum > With.reaction.runtimeQueueDuration) { estimationRuntimes.dequeue() }
    lastEstimationCompletion = With.frame
  }
}
