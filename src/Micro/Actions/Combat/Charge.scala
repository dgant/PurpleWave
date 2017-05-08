package Micro.Actions.Combat

import Information.Battles.TacticsTypes.Tactics
import Micro.Actions.Action
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

object Charge extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.unit.canMoveThisFrame &&
    (Yolo.active || intent.tactics.exists(_.has(Tactics.Movement.Charge)))
  }
  
  override def perform(intent: Intention) {
    intent.movementProfile = MovementProfiles.charge
  }
}
