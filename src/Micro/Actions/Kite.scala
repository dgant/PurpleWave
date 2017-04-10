package Micro.Actions
import Information.Battles.Simulation.Tactics.TacticMovement
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    Yolo.disabled &&
    intent.unit.canMove &&
    intent.tactics.exists(_.movement == TacticMovement.Kite)
  }
  
  override def perform(intent: Intention): Boolean = {
    intent.movementProfile = MovementProfiles.kite
    false
  }
}
