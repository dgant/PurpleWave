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
       intent.tactics.exists(_.has(Tactics.Movement.Flee))                     ||
      (intent.tactics.exists(_.has(Tactics.Wounded.Flee))  && wounded(intent)) ||
      (intent.tactics.exists(_.has(Tactics.Workers.Flee))  && worker(intent))
    ) &&
    intent.threats.nonEmpty
  }
  
  override def perform(intent: Intention): Boolean = {
    
    //If workers are fleeing to another base, let them
    if ( ! intent.toGather.exists(_.pixelDistanceSquared(intent.unit.pixelCenter) > 32.0 * 12)) {
      intent.movementProfile = MovementProfiles.flee
      intent.toGather = None
      intent.canAttack = false
    }
    
    false
  }
  
  def wounded(intent:Intention):Boolean = {
    intent.unit.totalHealth < Math.min(20, intent.unit.totalHealth / 3)
  }
  
  def worker(intent:Intention):Boolean = {
    intent.unit.unitClass.isWorker
  }
}
