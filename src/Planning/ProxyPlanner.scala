package Planning

import Information.Geography.Types.Zone
import Lifecycle.With

object ProxyPlanner {
  
  def proxyEnemyNatural: Option[Zone] = {
    With.geography.bases.find(_.isNaturalOf.exists( ! _.owner.isUs)).map(_.zone)
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
      return With.geography.ourNatural.map(_.zone)
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
