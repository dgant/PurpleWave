package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Minutes

object TargetFilterIgnoreScouts extends TargetFilter {
  
  // If we need to defend from real threats,
  // ignore scouting workers
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if (With.frame > Minutes(8)()) return true

    /*
      Chase if:
      * We are not in our base
      * We are being worker rushed, or
      * We can catch it and (there are no combat enemies or the worker is being violent)
     */
    lazy val targetIsWorker     = target.unitClass.isWorker
    lazy val targetInOurBase    = target.base.exists(_.owner.isUs)
    lazy val isEarlyGame        = With.frame < Minutes(6)()
    lazy val targetInEnemyBase  = target.base.exists(_.owner.isEnemy)
    lazy val inRange            = actor.inRangeToAttack(target)
    lazy val canCatch           = inRange || actor.pixelRangeAgainst(target) > 32.0 * 3.0 || actor.topSpeed > target.topSpeed * 1.25
    lazy val facingRealThreats  = actor.matchups.targets.exists(u => u.unitClass.attacksGround && ! u.unitClass.isWorker)
    lazy val beingWorkerRushed  = actor.matchups.targets.count(_.unitClass.isWorker) > 2 || With.fingerprints.workerRush.matches
    lazy val beingProxied       = actor.matchups.targets.exists(_.unitClass.isBuilding) || With.fingerprints.cannonRush.matches
    
    lazy val hasFormation       = actor.agent.toReturn.isDefined
    lazy val targetingWorker    = target.unitClass.isWorker
    lazy val combatThreats      = actor.matchups.enemies.exists(e => e.unitClass.attacksGround && ! e.unitClass.isWorker)
    lazy val targetViolent      = target.isBeingViolent
    
    if ( ! targetIsWorker)                    return true
    if (beingWorkerRushed)                    return true
    if (beingProxied)                         return true
    if (facingRealThreats && targetViolent)   return true
    if (isEarlyGame && ! targetInEnemyBase && ! inRange) return false
    
    canCatch
  }
  
}
