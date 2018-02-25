package Micro.Actions.Combat.Attacking.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterMission extends TargetFilter {
  
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    lazy val cleanup      = With.intelligence.firstEnemyMain.isDefined && With.geography.enemyBases.isEmpty
    lazy val pillaging    = actor.agent.canPillage || cleanup
    lazy val destination  = actor.agent.destination.zone
    lazy val targetZone   = target.zone
    lazy val arrived      = targetZone == destination || targetZone.bases.exists(_.owner == destination.owner)
    lazy val engaged      = actor.matchups.allies.exists(_.matchups.threatsInRange.nonEmpty)
    lazy val inRange      = actor.inRangeToAttack(target)
    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
  
    lazy val alliesAssisting  = target.matchups.threats.exists(ally =>
      (ally.topSpeed >= target.topSpeed || ally.pixelRangeAgainst(target) >= target.pixelRangeAgainst(ally))
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
  
    lazy val targetBusy       = target.gathering || target.constructing || target.repairing
    lazy val targetCatchable  = actor.topSpeed >= target.topSpeed || actor.inRangeToAttack(target) || targetBusy || alliesAssisting
    
    val output = (targetCatchable || atOurWorkers) && (pillaging || arrived || inRange || engaged)
    
    output
  }
  
}
