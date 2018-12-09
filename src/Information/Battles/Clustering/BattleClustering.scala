package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var nextUnits:          Traversable[UnitInfo] = Vector.empty
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Set.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Set.empty)
  
  //////////////////////
  // Batch processing //
  //////////////////////
  
  def clusters: Vector[Vector[UnitInfo]] = clusterComplete.clusters
  
  def enqueue(units: Set[UnitInfo]) {
    nextUnits = units
  }
  
  def run() {
    if (clusterInProgress.isComplete) {
      runtimes.enqueue(With.framesSince(lastClusterCompletion))
      while (runtimes.length > 10) runtimes.dequeue()
      lastClusterCompletion = With.frame
      clusterComplete       = clusterInProgress
      clusterInProgress     = new BattleClusteringState(nextUnits.toSet)
    }
    else {
      while ( ! clusterInProgress.isComplete && With.performance.continueRunning) {
        clusterInProgress.step()
      }
    }
  }
}
