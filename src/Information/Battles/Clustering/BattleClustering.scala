package Information.Battles.Clustering

import Information.Grids.Disposable.GridDisposableBoolean
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var nextUnits:          Vector[UnitInfo] = Vector.empty
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Vector.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Vector.empty)

  val exploredFriendly = new GridDisposableBoolean
  val exploredEnemy    = new GridDisposableBoolean

  //////////////////////
  // Batch processing //
  //////////////////////
  
  def clusters: Vector[Vector[UnitInfo]] = clusterComplete.clusters
  
  def enqueue(units: Vector[UnitInfo]) {
    nextUnits = units
  }
  
  def run() {
    if (clusterInProgress.isComplete) {
      runtimes.enqueue(With.framesSince(lastClusterCompletion))
      while (runtimes.length > 10) runtimes.dequeue()
      lastClusterCompletion = With.frame
      clusterComplete       = clusterInProgress
      clusterInProgress     = new BattleClusteringState(nextUnits)
      exploredFriendly.update()
      exploredEnemy.update()
      With.units.all.foreach(u => {
        u.clusteringEnabled = false
        u.clusterParent = None
        u.clusterChild = None
      })
      nextUnits.foreach(_.clusteringEnabled = true)
    }
    else {
      while ( ! clusterInProgress.isComplete && With.performance.continueRunning) {
        clusterInProgress.step()
      }
    }
  }
}
