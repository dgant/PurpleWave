package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Tactic.Squads.Squad
import Utilities.CountMap

final class Cluster(val units: UnorderedBuffer[UnitInfo] = new UnorderedBuffer[UnitInfo]()) {
  var hull                : Seq[UnitInfo] = Maff.convexHull(units, (u: UnitInfo) => u.pixel)
  var hullCentroid        : Option[Pixel] = None
  var strengthFriendly    : Double = _
  var strengthEnemy       : Double = _
  var strengthRatio       : Double = _
  var strengthRatioScale  : Double = _
  var friendlyEligible    : Boolean = _
  var enemyEligible       : Boolean = _
  var battleEligible      : Boolean = _
  var squadCounts         : CountMap[Squad] = new CountMap[Squad]()

  def hullExpanded: Seq[Pixel] = {
    measureCentroid()
    hull.view.map(_.pixel).map(p => hullCentroid.get.project(p, hullCentroid.get.pixelDistance(p) + With.battles.clustering.rangePixels))
  }
  def merge(other: Cluster): Unit = {
    hull = Maff.convexHull(hull ++ other.hull, (u: UnitInfo) => u.pixel)
    hullCentroid = None
    units.addAll(other.units)
  }
  def intersects(other: Cluster): Boolean = (
    Maff.convexPolygonsIntersect(hull.view.map(_.pixel), other.hullExpanded)
  )
  def atWar: Boolean = strengthRatioScale < 4.0

  def measureCentroid(): Unit = {
    hullCentroid = hullCentroid.orElse(Some(Maff.centroid(hull.view.map(_.pixel))))
  }
  def measureStrength(): Unit = {
    units.foreach(unit =>
      if (warEligible(unit)) {
        // TODO: We can't just use the unit's skim strength because units don't get one prior to skimulation.
        // Can we use a fast intermediate skim estimation instead?
        val strength = (2.0 - unit.injury) * unit.unitClass.skimulationValue
        if (unit.isFriendly) {
          strengthFriendly += strength
        } else {
          strengthEnemy += strength
        }
      })
    strengthRatio       = Maff.nanToInfinity(strengthFriendly / strengthEnemy)
    strengthRatioScale  = Math.max(strengthRatio, 1.0 / strengthRatio)
  }

  def measureBattleEligibility(): Unit = {
    friendlyEligible  = units.exists(u => u.isFriendly && warEligible(u))
    enemyEligible     = units.exists(u => u.isEnemy && warEligible(u))
    battleEligible    = atWar || (friendlyEligible && enemyEligible)
  }

  def measureSquads(squadEnemies: Map[Squad, Set[UnitInfo]]): Unit = {
    units.foreach(_.friendly.foreach(_.squad.foreach(squadCounts(_) += 1)))
    units.foreach(_.foreign.foreach(e => squadEnemies.foreach(p => if (p._2.contains(e)) squadCounts(p._1) += 1)))
  }

  @inline private def warEligible(unit: UnitInfo): Boolean = unit.unitClass.attacksOrCastsOrDetectsOrTransports
}