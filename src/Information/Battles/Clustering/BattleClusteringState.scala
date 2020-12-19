package Information.Battles.Clustering

import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class BattleClusteringState(val seedUnits: Vector[UnitInfo]) {

  private var _clusters: Option[Vector[Iterable[UnitInfo]]] = None
  def clusters: Vector[Iterable[UnitInfo]] = _clusters.getOrElse(Vector.empty)
  def isComplete: Boolean = _clusters.isDefined

  val enemyQueue: mutable.Queue[UnitInfo] = mutable.Queue[UnitInfo]()
  val friendlyQueue: mutable.Queue[UnitInfo] = new mutable.Queue[UnitInfo]()
  seedUnits.foreach(u => {
    if (u.isEnemy) enemyQueue += u else if (u.isFriendly) friendlyQueue += u
    u.clusteringEnabled = true
    u.clusteringRadiusSquared = u.effectiveRangePixels + 32 * 6
    u.clusteringRadiusSquared = u.clusteringRadiusSquared * u.clusteringRadiusSquared
    u.cluster = None
  })

  def step() {
    if (enemyQueue.nonEmpty) linkEnemy()
    else if (friendlyQueue.nonEmpty) linkFriendly()
    else if ( ! isComplete) compileClusters()
  }

  @inline private def link(a: UnitInfo, b: UnitInfo): Unit = {
    if (a.cluster.isDefined) {
      if (b.cluster.isDefined) {
        a.cluster.get.merge(b.cluster.get)
      } else {
        a.cluster.get.units += b
        b.cluster = a.cluster
      }
    } else if (b.cluster.isDefined) {
      b.cluster.get.units += a
      a.cluster = b.cluster
    } else {
      val newCluster = new BattleCluster
      newCluster.units += a
      newCluster.units += b
      a.cluster = Some(newCluster)
      b.cluster = a.cluster
    }
  }
  private def linkEnemy(): Unit = {
    val enemy = enemyQueue.dequeue()
    friendlyQueue.foreach(friendly => {
      val distanceSquared = enemy.pixelDistanceSquared(friendly)
      if (distanceSquared < enemy.clusteringRadiusSquared || distanceSquared < friendly.clusteringRadiusSquared) {
        link(enemy, friendly)
      }
    })
    enemyQueue.foreach(other => {
      if (enemy.unitClass.attacksOrCastsOrDetectsOrTransports || other.unitClass.attacksOrCastsOrDetectsOrTransports) {
        val distanceSquared = enemy.pixelDistanceSquared(other)
        if (distanceSquared < enemy.clusteringRadiusSquared || distanceSquared < other.clusteringRadiusSquared) {
          link(enemy, other)
        }
      }
    })
  }

  private def linkFriendly(): Unit = {
    val friendly = friendlyQueue.dequeue()
    friendlyQueue.foreach(other => {
      if (friendly.unitClass.attacksOrCastsOrDetectsOrTransports || other.unitClass.attacksOrCastsOrDetectsOrTransports) {
        val distanceSquared = friendly.pixelDistanceSquared(other)
        if (distanceSquared < friendly.clusteringRadiusSquared || distanceSquared < other.clusteringRadiusSquared) {
          link(friendly, other)
        }
      }
    })
  }

  private def compileClusters(): Unit = {
    _clusters = Some(seedUnits.view.flatMap(_.cluster).map(_.units).distinct.toVector)
  }
}
