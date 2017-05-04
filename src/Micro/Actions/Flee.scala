package Micro.Actions
import Information.Battles.TacticsTypes.Tactics
import Lifecycle.With
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

object Flee extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    Yolo.disabled &&
    intent.unit.canMoveThisFrame &&
    (
      (fightersFlee (intent)  && ! isWorker(intent))  ||
      (workersFlee  (intent)  &&   isWorker(intent))  ||
      (woundedFlee  (intent)  &&   isWounded(intent))
    ) &&
    intent.threats.nonEmpty
  }
  
  override def perform(intent: Intention): Boolean = {
    
    //Only stop gathering if immediately threatened and if continuing to mine isn't going to help
    if (isWorker(intent)
      && intent.toGather.nonEmpty
      && intent.threatsActive.exists(threat =>
        threat.pixelDistanceFast(intent.unit) >= threat.pixelDistanceFast(intent.toGather.get))) {
      return false
    }
    
    val enemyFaster = intent.threatsActive.exists(threat => threat.topSpeed > intent.unit.topSpeed)
    val weAreFaster = intent.threatsActive.forall(threat => threat.topSpeed < intent.unit.topSpeed)
    val weOutrange  = intent.threatsActive.forall(threat => threat.pixelRangeAgainst(intent.unit) < intent.unit.pixelRangeAgainst(threat))
    val threatsFar  = weOutrange && intent.threatsActive.forall(_.framesBeforeAttacking(intent.unit) > 0)
    
    intent.movementProfile = MovementProfiles.flee
    if (enemyFaster) {
      //No sense running anywhere than to help
      intent.movementProfile.preferOrigin += 2.0
    }
    intent.toGather = None
    intent.canAttack = enemyFaster || threatsFar
    intent.canPursue = weAreFaster && weOutrange
    
    val ourDistanceToOrigin = intent.unit.pixelDistanceTravelling(intent.origin)
    //Using travel distance for us and real distance for the enemy is intentional;
    //we don't want Mutalisks retreating over Marines, for example, just because the Marines have further to walk.
    if (intent.threatsActive.forall(_.pixelDistanceFast(intent.origin) >= ourDistanceToOrigin)) {
      With.commander.move(intent, intent.origin)
      return true
    }
    Move.consider(intent)
  }
  
  def fightersFlee  (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Movement.Flee))
  def woundedFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Wounded.Flee))
  def workersFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Workers.Flee))
  def workersFight  (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Workers.FightAll)) || intent.tactics.exists(_.has(Tactics.Workers.FightHalf))
  def isWounded     (intent:Intention)  : Boolean = intent.unit.wounded
  def isWorker      (intent:Intention)  : Boolean = intent.unit.unitClass.isWorker
}
