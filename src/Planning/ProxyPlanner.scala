package Planning

import Information.Geography.Types.Zone
import Lifecycle.With

object ProxyPlanner {

  def proxyAutomaticAggressive: Option[Zone] = {
    proxyEnemyNatural.orElse(proxyMiddle)
  }
  
  def proxyAutomaticSneaky: Option[Zone] = {
    proxyOutsideEnemyNatural.orElse(proxyMiddle)
  }
  
  def proxyAutomaticHatchery: Option[Zone] = {
    proxyEnemyNatural.orElse(proxyMiddleBase)
  }
  
  def proxyEnemyMain: Option[Zone] = {
    With.intelligence.enemyMain.map(_.zone)
  }
  
  def proxyEnemyNatural: Option[Zone] = {
    With.intelligence.enemyNatural.map(_.zone)
  }
  
  def proxyOutsideEnemyNatural: Option[Zone] = {
    proxyEnemyNatural.map(z => z.exit.map(_.otherSideof(z)).getOrElse(z))
  }
  
  def proxyMiddle: Option[Zone] = {
    val eligibleZones = With.geography.zones.filter(_.tilesBuildable.length > 60)
    proxyPreferredZone(eligibleZones)
  }
  
  def proxyMiddleBase: Option[Zone] = {
    val eligibleZones = With.geography.bases.map(_.zone).toSet.toSeq
    proxyPreferredZone(eligibleZones)
  }
  
  def proxyPreferredZone(eligibleZones: Iterable[Zone]): Option[Zone] = {
    if (eligibleZones.isEmpty) {
      return Some(With.geography.ourNatural.zone)
    }
    Some(
      eligibleZones.minBy(zone =>
        5 * With.geography.bases
          .filter(base => base.isStartLocation && ! base.owner.isFriendly)
          .map(_.heart.groundPixels(zone.centroid))
          .sum +
        3 * With.geography.ourBases
          .map(_.heart.groundPixels(zone.centroid))
          .sum))
  }
}
