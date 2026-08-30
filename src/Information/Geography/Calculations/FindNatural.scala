package Information.Geography.Calculations

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With

object FindNatural {

  private val localDistanceSlackTiles = 12
  private val openingWeight           = 1.25
  private val outwardWeight           = 2.0
  private val outwardMaximumTiles     = 12.0

  private case class Candidate(
    base          : Base,
    distanceMain  : Int,
    distanceEnemy : Int,
    route         : Vector[Zone])

  def apply(main: Base): Option[Base] = {
    val enemyMains = With.geography.mains.filterNot(main==)
    val candidates = With.geography.bases.view
      .filterNot(_.isMain)
      .filterNot(_.gas.isEmpty)
      .flatMap(base => With.paths.zonePath(main.zone, base.zone).map(path => Candidate(
        base,
        base.heart.groundTiles(main.heart),
        enemyMains.map(enemyMain => base.heart.groundTiles(enemyMain.heart)).min,
        path.zones)))
      .filter(candidate => candidate.distanceMain < candidate.distanceEnemy)
      .toVector

    if (candidates.isEmpty) return None

    val nearestDistance = candidates.map(_.distanceMain).min
    val localCandidates = candidates.filter(_.distanceMain <= nearestDistance + localDistanceSlackTiles)
    val directRouteLength = localCandidates.map(_.route.length).min
    val localEnemyDistanceMaximum = localCandidates
      .filter(_.route.length == directRouteLength)
      .map(_.distanceEnemy)
      .max

    Some(localCandidates.minBy(candidate => (
      score(main, candidate, directRouteLength, localEnemyDistanceMaximum),
      candidate.base.townHallTile.i)).base)
  }

  private def score(
    main                      : Base,
    candidate                 : Candidate,
    directRouteLength         : Int,
    localEnemyDistanceMaximum : Int)
      : Double = {

    val outwardBonusTiles =
      if (candidate.route.length == directRouteLength) {
        Math.min(outwardMaximumTiles, localEnemyDistanceMaximum - candidate.distanceEnemy)
      } else {
        0.0
      }

    candidate.distanceMain + openingWeight * protectedOpeningWidthTiles(main, candidate) - outwardWeight * outwardBonusTiles
  }

  private def protectedOpeningWidthTiles(main: Base, candidate: Candidate): Double = {
    val routeZones = candidate.route.toSet
    val otherMainZones = With.geography.mains.filterNot(main==).map(_.zone).toSet
    val deadEndZones = GetExits(routeZones)
      .flatMap(_.zones)
      .diff(routeZones)
      .diff(otherMainZones)
      .filter(_.exits.size == 1)

    GetExits(routeZones ++ deadEndZones).map(_.diameterPixels).sum / 32.0
  }
}
