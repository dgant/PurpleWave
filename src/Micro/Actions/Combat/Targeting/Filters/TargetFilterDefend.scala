package Micro.Actions.Combat.Targeting.Filters

import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Minutes

case class TargetFilterDefend(zone: Zone) extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val firingPixel = actor.pixelToFireAt(target)

    // If we want to attack anyway there's no need to ignore targets
    if (With.blackboard.wantToAttack()) return true

    // Expensive
    if (With.frame > Minutes(10)()) return true

    // Fire at enemies we can hit from inside the zone
    if (target.zone == zone) return true
    if (firingPixel.zone == zone && firingPixel.altitudeBonus >= actor.altitudeBonus) return true

    // Target enemies between us and the goal zone
    if (actor.inRangeToAttack(target) && actor.readyForAttackOrder) return true
    if (actor.zone != zone && actor.pixelDistanceTravelling(zone.centroid) > target.pixelDistanceTravelling(zone.centroid)) return true

    // Fire at enemies unavoidably threatening the zone (perhaps sieging it from outside)
    if (target.presumptiveTarget.exists(ally => ally.zone == zone && ally.visibleToOpponents)) return true

    false
  }
}
