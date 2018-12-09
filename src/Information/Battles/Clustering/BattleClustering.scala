package Information.Battles.Clustering

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var nextUnits:          Seq[UnitInfo] = Seq.empty
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Seq.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Seq.empty)
  
  //////////////////////
  // Batch processing //
  //////////////////////
  
  def clusters: Vector[Vector[UnitInfo]] = clusterComplete.clusters
  
  def enqueue(units: Seq[UnitInfo]) {
    nextUnits = units
  }
  
  def run() {
    if (clusterInProgress.isComplete) {
      runtimes.enqueue(With.framesSince(lastClusterCompletion))
      while (runtimes.length > 10) runtimes.dequeue()
      lastClusterCompletion = With.frame
      clusterComplete       = clusterInProgress
      clusterInProgress     = new BattleClusteringState(nextUnits)

      With.units.all.foreach(u => {
        u.clusteringEnabled = false
        u.clusterParent = None
        u.clusterChild = None
      })
      nextUnits.foreach(_.clusteringEnabled)
    }
    else {
      while ( ! clusterInProgress.isComplete && With.performance.continueRunning) {
        clusterInProgress.step()
      }
    }
  }
}
