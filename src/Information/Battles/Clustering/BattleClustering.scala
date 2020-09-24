package Information.Battles.Clustering

import Information.Battles.BattleClassificationFilters
import Information.Grids.Disposable.GridDisposableBoolean
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Vector.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Vector.empty)

  val exploredFriendly = new GridDisposableBoolean
  val exploredEnemy    = new GridDisposableBoolean
  
  def clusters: Vector[Vector[UnitInfo]] = clusterComplete.clusters

  def isComplete: Boolean = clusterInProgress.isComplete

  def reset() {
    val nextUnits = With.units.playerOwned.view.filter(BattleClassificationFilters.isEligibleLocal).toVector
    clusterInProgress = new BattleClusteringState(nextUnits)

    With.units.playerOwned.foreach(_.clusteringEnabled = false)
    nextUnits.foreach(_.clusteringEnabled = true)

    exploredFriendly.update()
    exploredEnemy.update()
  }
  
  def step() {
    clusterInProgress.step()
  }

  def publish(): Unit = {
    // Record runtime
    runtimes.enqueue(With.framesSince(lastClusterCompletion))
    while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
    lastClusterCompletion = With.frame

    // Evaluate the lazy clusters before we wipe away the data required to produce them
    clusterComplete.clusters
    clusterComplete = clusterInProgress
  }
}
