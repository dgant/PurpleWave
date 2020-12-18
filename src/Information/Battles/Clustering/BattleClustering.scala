package Information.Battles.Clustering

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.{BattleLocal, Team}
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Vector.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Vector.empty)

  def clusters: Vector[Iterable[UnitInfo]] = clusterComplete.clusters
  def isComplete: Boolean = clusterInProgress.isComplete

  def reset() {
    With.units.playerOwned.foreach(_.clusteringEnabled = false)
    val nextUnits = With.units.playerOwned.view.filter(BattleClassificationFilters.isEligibleLocal).toVector
    clusterInProgress = new BattleClusteringState(nextUnits)
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
    val battlesFromClusters = clusters
      .view
      .map(cluster =>
        new BattleLocal(
          new Team(cluster.view.filter(_.isOurs).toVector),
          new Team(cluster.view.filter(_.isEnemy).toVector)))
      .filter(_.teams.forall(_.units.exists(u =>
        u.canAttack
        || u.unitClass.rawCanAttack
        || u.unitClass.isSpellcaster
        || u.unitClass.isDetector
        || u.unitClass.isTransport)))
      .toVector
    With.battles.nextBattlesLocal = battlesFromClusters

    // TODO: These can probably be done separately, and the global battle probably doesn't even need a focus
    With.battles.nextBattlesLocal.foreach(_.updateFoci())
  }
}
