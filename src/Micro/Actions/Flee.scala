package Micro.Actions
import Information.Battles.TacticsTypes.Tactics
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
      && ! intent.threats.exists(threat =>
        threat.target.contains(intent.unit)
        && threat.pixelDistanceFast(intent.unit) <= threat.pixelImpactAgainst(8, intent.unit)
        && threat.pixelDistanceFast(intent.unit) >= threat.pixelDistanceFast(intent.toGather.get))) {
      return false
    }
    
    intent.movementProfile = MovementProfiles.flee
    intent.toGather = None
    intent.canAttack = false
    
    Move.consider(intent)
  }
  
  def fightersFlee  (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Movement.Flee))
  def woundedFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Wounded.Flee))
  def workersFlee   (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Workers.Flee))
  def workersFight  (intent:Intention)  : Boolean = intent.tactics.exists(_.has(Tactics.Workers.FightAll)) || intent.tactics.exists(_.has(Tactics.Workers.FightHalf))
  def isWounded     (intent:Intention)  : Boolean = intent.unit.wounded
  def isWorker      (intent:Intention)  : Boolean = intent.unit.unitClass.isWorker
}
