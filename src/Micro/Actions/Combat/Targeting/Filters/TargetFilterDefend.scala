package Micro.Actions.Combat.Targeting.Filters

import Information.Geography.Types.Zone
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

case class TargetFilterDefend(zone: Zone) extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val firingPixel = actor.pixelToFireAt(target)

    // Fire at enemies we can hit from inside the zone
    if (target.zone == zone) return true
    if (firingPixel.zone == zone && firingPixel.altitudeBonus >= actor.altitudeBonus) return true

    // Target enemies between us and the goal zone
    if (actor.inRangeToAttack(target) && actor.readyForAttackOrder) return true
    if (actor.zone != zone && actor.pixelDistanceTravelling(zone.centroid) < target.pixelDistanceTravelling(zone.centroid)) return true

    // Fire at enemies unavoidably threatening the zone (perhaps sieging it from outside)
    if (target.presumptiveTarget.exists(ally => ally.zone == zone && ally.visibleToOpponents)) return true

    false
  }
}
