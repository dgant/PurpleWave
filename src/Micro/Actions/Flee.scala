package Micro.Actions
import Information.Battles.Simulation.Tactics.{TacticMovement, TacticWorkers, TacticWounded}
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

object Flee extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    Yolo.disabled &&
    intent.unit.canMove &&
    (
      (intent.tactics.exists(_.movement == TacticMovement.Flee))                                            ||
      (intent.tactics.exists(_.wounded  == TacticWounded.Flee)       && wounded(intent))                    ||
      (intent.tactics.exists(_.wounded  == TacticWounded.FleeRanged) && wounded(intent) && ranged(intent))  ||
      (intent.tactics.exists(_.workers  == TacticWorkers.Flee)       && worker(intent))
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
  
  def ranged(intent:Intention):Boolean = {
    intent.unit.unitClass.maxAirGroundRange > 20
  }
  
  def worker(intent:Intention):Boolean = {
    intent.unit.unitClass.isWorker
  }
}
