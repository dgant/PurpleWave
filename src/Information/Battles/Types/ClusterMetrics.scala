package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.Squad
import Utilities.CountMap

final class ClusterMetrics(val cluster: Cluster) {
  lazy val squadCounts: CountMap[Squad] = {
    val output = new CountMap[Squad]()
    cluster.units.foreach(_.friendly.foreach(_.squad.foreach(output(_) += 1)))
    cluster.units.foreach(_.foreign.foreach(e => With.squads.enemies.foreach(p => if (p._2.contains(e)) output(p._1) += 1)))
    output
  }

  lazy val hull             : Seq[Pixel]  = Maff.convexHull(cluster.units.view.map(_.pixel))
  lazy val hullExpanded     : Seq[Pixel]  = Maff.convexHull(hull.view.flatMap(_.expand(With.battles.clustering.rangePixels)))
  lazy val hullCentroid     : Pixel       = Maff.centroid(hull)
  lazy val friendlyEligible : Boolean     = cluster.units.exists(u => u.isFriendly  && warEligible(u))
  lazy val enemyEligible    : Boolean     = cluster.units.exists(u => u.isEnemy     && warEligible(u))
  lazy val strengthFriendly : Double      = cluster.units.view.filter(_.isFriendly).map(unitStrength).sum
  lazy val strengthEnemy    : Double      = cluster.units.view.filter(_.isEnemy)   .map(unitStrength).sum

  // TODO: We can't just use the unit's skim strength because units don't get one prior to skimulation.
  // Can we use a fast intermediate skim estimation instead?
  @inline def unitStrength(unit: UnitInfo): Double  = (2.0 - unit.injury) * unit.unitClass.skimulationValue
  @inline def warEligible (unit: UnitInfo): Boolean = unit.unitClass.attacksOrCastsOrDetectsOrTransports
}
