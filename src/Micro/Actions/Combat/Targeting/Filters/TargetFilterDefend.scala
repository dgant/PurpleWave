package Micro.Actions.Combat.Targeting.Filters

import Information.Geography.Types.Zone
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Minutes

case class TargetFilterDefend(zone: Zone) extends TargetFilter {
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {

    // If we want to attack anyway there's no need to ignore targets
    if (With.blackboard.wantToAttack()) return true

    // Expensive
    if (With.frame > Minutes(10)()) return true

    // Fire at enemies we can hit from inside the zone
    if (target.zone == zone) return true

    val firingPixel = actor.pixelToFireAt(target)
    if (firingPixel.zone == zone
      && firingPixel.altitude >= actor.agent.toReturn.getOrElse(actor.pixel).altitude
      && actor.inRangeToAttackFrom(target, firingPixel) // TODO: Walkability constraints can produce a firing pixel that's not actually in range
      && ! zone.edges.exists(_.contains(firingPixel))) return true

    // Target enemies between us and the goal zone
    if (actor.inRangeToAttack(target) && actor.readyForAttackOrder) return true
    if (actor.zone != zone && actor.pixelDistanceTravelling(zone.centroid) > target.pixelDistanceTravelling(zone.centroid)) return true

    // Fire at enemies unavoidably threatening the zone (perhaps sieging it from outside)
    if (target.presumptiveTarget.exists(ally =>
      ally != actor
      && ally.zone == zone
      && ally.visibleToOpponents
      && (ally.unitClass.melee || ! zone.edges.exists(_.contains(ally.pixel))))) return true

    false
  }
}
