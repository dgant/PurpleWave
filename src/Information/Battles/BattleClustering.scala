package Information.Battles

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class BattleClustering {

  val runtimes = new mutable.Queue[Int]

  var rangePixels         : Int = _
  var rangePixelsSquared  : Int = _
  var lastCompletion      : Int = _

  private class Cluster(val units: ArrayBuffer[UnitInfo] = new ArrayBuffer[UnitInfo]()) {
    var hull          : Seq[UnitInfo] = Maff.convexHull(units, (u: UnitInfo) => u.pixel)
    var hullCentroid  : Option[Pixel] = None

    def hullExpanded: Seq[Pixel] = {
      hullCentroid = hullCentroid.orElse(Some(Maff.centroid(hull.view.map(_.pixel))))
      hull.view.map(_.pixel).map(p => hullCentroid.get.project(p, hullCentroid.get.pixelDistance(p) + rangePixels))
    }
    def merge(other: Cluster): Unit = {
      hull = Maff.convexHull(hull ++ other.hull, (u: UnitInfo) => u.pixel)
      hullCentroid = None
      units ++= other.units
    }
    def intersects(other: Cluster): Boolean = (
      Maff.convexPolygonsIntersect(hull.view.map(_.pixel), other.hullExpanded)
    )
  }

  def recalculate(): Unit = {
    val tankRange       = ?(Players.all.exists(Terran.SiegeMode(_)), 32 * 11, 0)
    rangePixels         = 32 * 5 + (Seq(tankRange).view ++ With.units.playerOwned.map(_.effectiveRangePixels.toInt)).max
    rangePixelsSquared  = rangePixels * rangePixels

    // Cluster units, as fixed-radius nearest neighbors.
    // This solution is not optimal but is faster to implement than a KD tree
    //
    // Step 1: Do a super-fast approximate clustering
    // This clustering doesn't guarantee full merging of clusters but in practice comes very close
    val fastClusters = new UnorderedBuffer[UnitInfo]()
    fastClusters.addAll(With.units.playerOwned.view.filter(BattleFilters.local))
    fastClusters.foreach(_.nextInCluster = None)
    var i = 0
    while(i < fastClusters.length) {
      val u1 = fastClusters(i)
      var j = i + 1
      while (j < fastClusters.length) {
        val u2 = fastClusters(j)
        if (u1.pixelDistanceSquared(u2) < rangePixelsSquared) {
          u2.nextInCluster = u1.nextInCluster
          u1.nextInCluster = Some(u2)
          fastClusters.removeAt(j)
        } else {
          j += 1
        }
      }
      i += 1
    }

    // Step 2: Merge our raw clusters in slower but guaranteed fashion
    val clusters = new UnorderedBuffer(fastClusters.view.map(u => {
      val output = new ArrayBuffer[UnitInfo]
      var next: Option[UnitInfo] = Some(u)
      while (next.isDefined) {
        output ++= next
        next = next.get.nextInCluster
      }
      new Cluster(output)
    }))
    var m = 0
    while(m < clusters.length) {
      var n = m + 1
      while (n < clusters.length) {
        if (clusters(m).intersects(clusters(n))) {
          clusters(m).merge(clusters(n))
          clusters.removeAt(n)

        } else {
          n += 1
        }
      }
      m += 1
    }

    // Publish clusters as battles
    With.battles.nextBattlesLocal = clusters
      .view
      .map(cluster =>
        new Battle(
          cluster.units.view.filter(_.isOurs),
          cluster.units.view.filter(_.isEnemy),
          isGlobal = false))
      .filter(_.teams.forall(_.units.exists(_.unitClass.attacksOrCastsOrDetectsOrTransports)))
      .toVector

    // Record runtime
    runtimes.enqueue(With.framesSince(lastCompletion))
    while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
    lastCompletion = With.frame
  }
}
