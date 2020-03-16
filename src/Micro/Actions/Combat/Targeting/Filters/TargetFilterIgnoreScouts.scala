package Micro.Actions.Combat.Targeting.Filters

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterIgnoreScouts extends TargetFilter {
  
  // If we need to defend from real threats,
  // ignore scouting workers
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    /*
      Chase if:
      * We are not in our base
      * We are being worker rushed, or
      * We can catch it and (there are no combat enemies or the worker is being violent)
     */
    lazy val targetIsWorker     = target.unitClass.isWorker
    lazy val targetInOurBase    = target.base.exists(_.owner.isUs)
    lazy val isEarlyGame        = With.frame < GameTime(6, 0)()
    lazy val targetInEnemyBase  = target.base.exists(_.owner.isEnemy)
    lazy val inRange            = actor.inRangeToAttack(target)
    lazy val canCatch           = inRange || actor.pixelRangeAgainst(target) > 32.0 * 3.0 || actor.topSpeed > target.topSpeed * 1.25
    lazy val facingRealThreats  = actor.matchups.targets.exists(u => u.unitClass.attacksGround && ! u.unitClass.isWorker)
    lazy val beingWorkerRushed  = actor.matchups.targets.count(_.unitClass.isWorker) > 2
    lazy val beingProxied       = actor.matchups.targets.exists(_.unitClass.isBuilding)
    
    lazy val hasFormation       = actor.agent.toForm.isDefined
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
