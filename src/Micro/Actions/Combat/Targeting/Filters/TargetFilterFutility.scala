package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {
  
  private def catchableBy(actor: UnitInfo, target: UnitInfo): Boolean = {
    lazy val targetBusy = target.gathering || target.constructing || target.repairing || ! target.canMove
    actor.topSpeed >= target.topSpeed || actor.inRangeToAttack(target) || targetBusy || actor.is(Zerg.Scourge)
  }
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
    lazy val alliesAssisting  = target.matchups.threats.exists(ally =>
      ally != actor
      && catchableBy(ally, target)
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
    
    lazy val targetCatchable  = catchableBy(actor, target) || alliesAssisting
    lazy val targetReachable  = target.visible || actor.flying || ! target.flying || With.grids.walkableTerrain.get(target.tileIncludingCenter)
    
    val output = targetReachable && (targetCatchable || atOurWorkers)
    
    output
  }
  
}
