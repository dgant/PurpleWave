package Information.Battles.Clustering

import Information.Battles.BattleClassificationFilters
import Information.Battles.Types.{BattleLocal, Team}
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClustering {

  class Cluster {
    var units: mutable.HashSet[UnitInfo] = new mutable.HashSet[UnitInfo]
    def merge(other: Cluster): Unit = {
      val useOurUnits = units.size > other.units.size
      val newUnits = if (useOurUnits) units else other.units
      val oldUnits = if (useOurUnits) other.units else units
      newUnits ++= oldUnits
      units = newUnits
      other.units = newUnits
    }
    override def equals(other: scala.Any): Boolean = other.isInstanceOf[Cluster] && other.asInstanceOf[Cluster].units.eq(units)
  }
  
  var lastClusterCompletion = 0
  val runtimes = new mutable.Queue[Int]
  
  private var clusterInProgress:  BattleClusteringState = new BattleClusteringState(Set.empty)
  private var clusterComplete:    BattleClusteringState = new BattleClusteringState(Set.empty)

  def clusters: Iterable[Seq[UnitInfo]] = clusterComplete.clusters
  def isComplete: Boolean = clusterInProgress.isComplete

  def reset() {
    val nextUnits = With.units.playerOwned.view.filter(BattleClassificationFilters.isEligibleLocal).toSet
    clusterInProgress = new BattleClusteringState(nextUnits)

    With.units.playerOwned.foreach(_.clusteringEnabled = false)
    nextUnits.foreach(_.clusteringEnabled = true)
    nextUnits.foreach(_.clusteringFound = false)
    nextUnits.foreach(_.cluster = None)
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
