package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Minutes

object TargetFilterFutility extends TargetFilter {

  // Ignore targets we have no chance of killing
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.canMove && ! actor.inRangeToAttack(target)) return false

    if (actor.inRangeToAttack(target)) return true

    // Respect PushKiters (intended for eg. Proxy Zealot rushes coming up against Dragoons)
    if (With.blackboard.pushKiters.get && target.canAttack(actor) && target.pixelRangeAgainst(actor) > 32) return true

    // This is a pretty expensive filter; avoid using it if possible
    if (With.frame > Minutes(15)()) return true

    if (actor.is(Terran.Vulture) && target.unitClass.isBuilding && ! target.unitClass.canAttack && With.frame < Minutes(4)()) return false

    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
    lazy val alliesAssisting = target.matchups.catchers.exists(ally =>
      ally != actor
      && (ally.topSpeed >= target.topSpeed || ally.pixelRangeAgainst(target) >= actor.pixelRangeAgainst(target))
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
    
    lazy val targetCatchable = target.battle.isEmpty || actor.topSpeed >= target.topSpeed || target.matchups.catchers.contains(actor) || alliesAssisting
    lazy val targetReachable  = (
      target.visible
      || actor.flying
      || ! target.flying
      || Vector(actor.pixelToFireAt(target).tileIncluding, target.tileIncludingCenter)
        .exists(tile =>
          With.grids.walkableTerrain.get(tile)
          && With.grids.altitudeBonus.get(tile) >= With.grids.altitudeBonus.get(target.tileIncludingCenter)))

    val output = targetReachable && (atOurWorkers || targetCatchable)
    
    output
  }
  
}
