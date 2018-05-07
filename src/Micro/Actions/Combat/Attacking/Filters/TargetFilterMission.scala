package Micro.Actions.Combat.Attacking.Filters

import Lifecycle.With
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterMission extends TargetFilter {
  
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cleanup      = With.intelligence.firstEnemyMain.isDefined && With.geography.enemyBases.isEmpty
    lazy val pillaging    = actor.agent.canPillage || cleanup
    lazy val destination  = actor.agent.destination.zone
    lazy val targetZone   = target.battle.map(_.enemy.centroid.zone).getOrElse(target.zone)
    lazy val arrived      = targetZone == destination || ( ! destination.owner.isNeutral && targetZone.bases.exists(_.owner == destination.owner))
    lazy val engaged      = actor.matchups.allies.exists(_.matchups.threatsInRange.nonEmpty)
    lazy val inRange      = actor.matchups.targetsInRange.nonEmpty
    lazy val isNearbyMine = target.is(Terran.SpiderMine) && ! target.effectivelyCloaked && actor.pixelRangeAgainst(target) > 96.0 && actor.pixelDistanceCenter(target) < 32.0 * 12.0
    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
  
    lazy val alliesAssisting  = target.matchups.threats.exists(ally =>
      ally != actor
      && ally.unitClass.orderable
      && (ally.topSpeed >= target.topSpeed || ally.topSpeed > actor.topSpeed || ally.inRangeToAttack(target) )
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
  
    lazy val targetBusy       = target.gathering || target.constructing || target.repairing
    lazy val targetCatchable  = actor.topSpeed >= target.topSpeed || actor.inRangeToAttack(target) || targetBusy || alliesAssisting
    lazy val targetReachable  = target.visible || actor.flying || ! target.flying || With.grids.walkableTerrain.get(target.tileIncludingCenter)
    
    val output = targetReachable && (targetCatchable || atOurWorkers) // && (pillaging || arrived || inRange || engaged || isNearbyMine)
    
    output
  }
  
}
