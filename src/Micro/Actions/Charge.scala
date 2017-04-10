package Micro.Actions
import Information.Battles.Simulation.Tactics.TacticMovement
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

object Charge extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canMove &&
    (
      Yolo.enabled || intent.tactics.exists(_.movement == TacticMovement.Charge)
    )
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.movementProfile = MovementProfiles.charge
    intent.targetProfile.preferInRange = 1.0
    intent.targetProfile.avoidDistance += 1.0
    false
  }
}
