package Information.Battles.Types

import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Tactic.Squads.Squad
import Utilities.CountMap

final class Cluster(val units: UnorderedBuffer[UnitInfo] = new UnorderedBuffer[UnitInfo]()) {

  var metrics: ClusterMetrics = new ClusterMetrics(this)

  def invalidateMetrics(): Unit = {
    new ClusterMetrics(this)
  }

  def absorb(other: Cluster): Unit = {
    if (other == this) {
      return
    }
    invalidateMetrics()
    units.addAll(other.units)
    other.units.clear()
  }

  def intersects(other: Cluster): Boolean = (
    Maff.convexPolygonsIntersect(metrics.hull, other.metrics.hullExpanded)
  )

  def squadCounts         : CountMap[Squad] = metrics.squadCounts
  def hull                : Seq[Pixel]      = metrics.hull
  def hullExpanded        : Seq[Pixel]      = metrics.hullExpanded
  def hullCentroid        : Pixel           = metrics.hullCentroid
  def friendlyEligible    : Boolean         = metrics.friendlyEligible
  def enemyEligible       : Boolean         = metrics.enemyEligible
  def strengthFriendly    : Double          = metrics.strengthFriendly
  def strengthEnemy       : Double          = metrics.strengthEnemy

  def strengthRatio       : Double          = Maff.nanToInfinity(metrics.strengthFriendly / metrics.strengthEnemy)
  def strengthRatioScale  : Double          = Math.max(strengthRatio, 1.0 / strengthRatio)
  def atWar               : Boolean         = strengthRatioScale < 1000.0
  def battleEligible      : Boolean         = atWar || (metrics.friendlyEligible && metrics.enemyEligible)

  override def toString: String = f"Cluster @ ${metrics.hullCentroid}: ${units.mkString}"
}