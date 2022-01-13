package Information.Battles

import Information.Battles.Types.BattleLocal
import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClustering {
  private val clusterDistanceTiles = 16
  private val clusterDistancePixels = 32 * clusterDistanceTiles
  private val clusterDistanceSquared: Int = clusterDistancePixels * clusterDistancePixels

  private val _clusters = new ArrayBuffer[ArrayBuffer[UnitInfo]]()
  var lastCompletion = 0
  val runtimes = new mutable.Queue[Int]

  def clusters: Seq[Seq[UnitInfo]] = _clusters

  def recalculate() {
    _clusters.clear()
    val unclustered = new UnorderedBuffer[UnitInfo]()
    unclustered.addAll(With.units.playerOwned.view.filter(BattleClassificationFilters.isEligibleLocal))
    unclustered.foreach(_.nextInCluster = None)
    var i = 0
    while(i < unclustered.length) {
      val u1 = unclustered(i)
      var j = i + 1
      while (j < unclustered.length) {
        val u2 = unclustered(j)
        if (u1.pixelDistanceSquared(u2) < clusterDistanceSquared) {
          u2.nextInCluster = u1.nextInCluster
          u1.nextInCluster = Some(u2)
          unclustered.removeAt(j)
        } else {
          j += 1
        }
      }
      i += 1
    }

    unclustered.foreach(u => {
      val cluster = new ArrayBuffer[UnitInfo]()
      _clusters += cluster
      var next: Option[UnitInfo] = Some(u)
      while (next.isDefined) {
        cluster ++= next
        next = next.get.nextInCluster
      }
    })

    // Publish clusters as battles
    With.battles.nextBattlesLocal = clusters
      .view
      .map(cluster =>
        new BattleLocal(
          cluster.view.filter(_.isOurs).toVector,
          cluster.view.filter(_.isEnemy).toVector))
      .filter(_.teams.forall(_.units.exists(_.unitClass.attacksOrCastsOrDetectsOrTransports)))
      .toVector

    // Record runtime
    runtimes.enqueue(With.framesSince(lastCompletion))
    while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
    lastCompletion = With.frame
  }
}
