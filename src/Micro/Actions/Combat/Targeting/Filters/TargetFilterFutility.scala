package Micro.Actions.Combat.Targeting.Filters

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {

  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.canMove && ! actor.inRangeToAttack(target)) {
      return false
    }

    // Respect PushKiters (intended for eg. Proxy Zealot rushes coming up against Dragoons)
    if (With.blackboard.pushKiters.get && target.canAttack(actor) && target.pixelRangeAgainst(actor) > 32) {
      return true
    }

    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
    lazy val alliesAssisting = target.matchups.catchers.exists(ally =>
      ally != actor
      && (ally.topSpeed >= target.topSpeed || ally.pixelRangeAgainst(target) >= actor.pixelRangeAgainst(target))
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
    
    lazy val targetCatchable  = target.matchups.catchers.contains(actor) || alliesAssisting
    lazy val targetReachable  = (
      target.visible
      || actor.flying
      || ! target.flying
      || Vector(actor.pixelToFireAt(target).tileIncluding, target.tileIncludingCenter)
        .exists(tile =>
          With.grids.walkableTerrain.get(tile)
          && With.grids.altitudeBonus.get(tile) >= With.grids.altitudeBonus.get(target.tileIncludingCenter)))

    if (actor.is(Terran.Vulture) && target.unitClass.isBuilding && With.frame < GameTime(0, 4)()) return false

    val output = targetReachable && (atOurWorkers || targetCatchable)
    
    output
  }
  
}
