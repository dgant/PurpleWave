package Information.Battles

import Information.Battles.Types.{Battle, Cluster}
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.?

import scala.collection.mutable

class BattleClustering {

  val runtimes = new mutable.Queue[Int]

  var speed               : Double = _
  var speedRange          : Int = _
  var tankRange           : Int = _
  var rangePixels         : Int = _
  var rangePixelsSquared  : Int = _
  var lastCompletion      : Int = _

  val fastClusters    = new UnorderedBuffer[UnitInfo]()
  val clusters        = new UnorderedBuffer[Cluster]()
  val clustersAtWar   = new UnorderedBuffer[Cluster]()
  val clustersAtPeace = new UnorderedBuffer[Cluster]()

  def recalculate(): Unit = {
    speed               = Maff.max(With.units.playerOwned.map(_.topSpeed)).getOrElse(0)
    speedRange          = (With.reaction.clusteringMax * speed).toInt
    tankRange           = ?(Players.all.exists(Terran.SiegeMode(_)), 32 * 11, 0)
    rangePixels         = 2 * speedRange + Math.max(tankRange, With.units.playerOwned.map(_.effectiveRangePixels).max.toInt)
    rangePixelsSquared  = rangePixels * rangePixels

    // Cluster units, as fixed-radius nearest neighbors.
    // This solution is not optimal but is faster to implement than a KD tree
    //
    // Step 1: Do a super-fast approximate clustering
    // This clustering doesn't guarantee full merging of clusters but in practice comes very close
    fastClusters.clear()
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
    clusters.clear()
    clusters.addAll(fastClusters.view.map(u => {
      val output = new UnorderedBuffer[UnitInfo]
      var next: Option[UnitInfo] = Some(u)
      while (next.isDefined) {
        output.addAll(next)
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

    identifyPeacefulAndWarlikeClusters()

    // Step 3: Try inventing a war if none exists
    // If the granularity of our clustering has meant that there are *no* wars
    if (clustersAtWar.isEmpty) {
      val clusterFriendly = Maff.maxBy(clustersAtPeace.filter(_.friendlyEligible))(_.units.count(_.isFriendly))
      val clusterEnemy    = Maff.maxBy(clustersAtPeace.filter(_.enemyEligible))   (_.units.count(_.isEnemy))
      clusterFriendly.foreach(friendly =>
        clusterEnemy.foreach(enemy => {
          friendly.merge(enemy)
          clusters.remove(enemy)
          identifyPeacefulAndWarlikeClusters()
        }))
    }


    // Step 4: Depopulate peaceful clusters
    //
    // We want clustering to be granular where appropriate and coarse elsewhere.
    // If an army has two halves, one of which should fight and the other of which should retreat, we want to see that.
    //
    // But excessive granularity can cause relevant army units to be omitted from a cluster.
    // So after granular clustering, we transfer units from irrelevant clusters to relevant clusters
    //
    // Identify squad-consensus battles in order to bias units towards relevant clusters
    val squadEnemies = With.squads.all.filter(_.enemies.nonEmpty).map(s => (s, s.enemies.toSet)).toMap
    clustersAtWar.foreach(_.measureCentroid())
    clustersAtWar.foreach(_.measureSquads(squadEnemies))

    // Transfer units from peace clusters to war clusters
    clustersAtPeace.foreach(peaceCluster => {
      var i = 0
      while (i < peaceCluster.units.length) {
        val unit = peaceCluster.units(i)
        var bestCluster: Option[Cluster] = None

        // We could potentially restrict this to just combat units, for performance if nothing else
        if (unit.isFriendly) {
          val squadCluster      = unit.friendly.flatMap(_.squad).flatMap(s => Maff.maxBy(clustersAtWar.filter(_.squadCounts.contains(s)))(_.squadCounts(s)))
          bestCluster           = squadCluster.orElse(Maff.minBy(clustersAtWar)(_.hullCentroid.map(unit.pixelDistanceTravelling).get))
        } else {
          val eligibleClusters  = Maff.orElse(clustersAtWar.filter(_.squadCounts.keys.exists(squadEnemies.get(_).exists(_.contains(unit)))), clustersAtWar)
          bestCluster           = Maff.minBy(eligibleClusters)(_.hullCentroid.map(unit.pixelDistanceTravelling).get)
        }

        bestCluster.foreach(warCluster => {
          peaceCluster.units.remove(unit)
          warCluster.units.add(unit)
          i -= 1
        })
        i += 1
      }
    })

    // Re-assess peace clusters; some may still be eligible for battles
    clustersAtPeace.foreach(_.measureBattleEligibility())


    // Step 4: Generate battles based on the clusters
    With.battles.nextBattlesLocal = (clustersAtWar.view ++ clustersAtPeace.view.filter(_.battleEligible))
      .map(cluster =>
        new Battle(
          cluster.units.view.filter(_.isOurs),
          cluster.units.view.filter(_.isEnemy),
          isGlobal = false))
      .toVector

    // Record runtime
    runtimes.enqueue(With.framesSince(lastCompletion))
    while (runtimes.sum > With.reaction.runtimeQueueDuration) { runtimes.dequeue() }
    lastCompletion = With.frame
  }

  private def identifyPeacefulAndWarlikeClusters(): Unit = {
    clustersAtWar  .clear()
    clustersAtPeace.clear()
    clusters.foreach(c => {
      c.measureStrength()
      ?(c.atWar, clustersAtWar, clustersAtPeace).add(c)
    })

  }
}
