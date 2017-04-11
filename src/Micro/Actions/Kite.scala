package Micro.Actions
import Information.Battles.Simulation.Tactics.TacticMovement
import Micro.Behaviors.MovementProfiles
import Micro.Intent.Intention
import Planning.Yolo

import Utilities.EnrichPosition._

object Kite extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    Yolo.disabled &&
    intent.unit.canMove &&
    intent.tactics.exists(_.movement == TacticMovement.Kite)
  }
  
  override def perform(intent: Intention): Boolean = {
    val framesToLookAhead = 8//Math.max(24, 2 * With.performance.frameDelay(1))
    //This interpretation of kiting doesn't quite line up with the battle simulator, which may cause unintended behavior.
    if (intent.threats.exists(threat =>
      threat.pixelDistanceSquared(intent.unit) < threat.project(framesToLookAhead).pixelDistanceSquared(intent.unit.pixelCenter) &&
      threat.pixelReachAgainst(framesToLookAhead, intent.unit) <= threat.pixelDistanceFast(intent.unit))) {
      return Flee.perform(intent)
    }
    if (intent.threats.forall(threat => threat.rangeAgainst(intent.unit) >= intent.unit.rangeAgainst(threat))) {
      return Flee.perform(intent)
    }
    
    intent.movementProfile = MovementProfiles.kite
    false
  }
}
