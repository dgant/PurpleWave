package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

class BattleClustering {
  
  var lastClusterCompletion = 0
  
  private var nextUnits:          Traversable[UnitInfo] = Vector.empty
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Set.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Set.empty)
  
  //////////////////////
  // Batch processing //
  //////////////////////
  
  def clusters: Vector[Set[UnitInfo]] = clusterComplete.clusters
  
  def enqueue(units: Traversable[UnitInfo]) {
    nextUnits = units
  }
  
  def run() {
    if (clusterInProgress.isComplete) {
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
