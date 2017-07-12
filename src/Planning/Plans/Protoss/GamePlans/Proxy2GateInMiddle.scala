package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With

class Proxy2GateInMiddle extends AbstractProxy2Gate {
  override protected def proxyZone: Option[Zone] = {
    val eligibleZones = With.geography.zones.filter(_.tilesBuildable.length > 60)
    if (eligibleZones.isEmpty) {
      return With.geography.ourNatural.map(_.zone)
    }
    Some(
      eligibleZones.minBy(zone =>
        5 * With.geography.bases
          .filter(base => base.isStartLocation && ! base.owner.isFriendly)
          .map(_.heart.groundPixelsByTile(zone.centroid))
          .sum +
        3 * With.geography.ourBases
          .map(_.heart.groundPixelsByTile(zone.centroid))
          .sum))
  }
}
