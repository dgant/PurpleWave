package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Movement.{EvaluatePositions, MovementProfile}
import Micro.Targeting.{EvaluateTargets, TargetingProfile}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
  
    val target = EvaluateTargets.best(intent, intent.targetProfile, intent.targets)
    
    if (intent.unit.cooldownRemaining < With.game.getRemainingLatencyFrames) {
      if (target.isDefined) {
        return With.commander.attack(intent, target.get)
      }
    }
    
    if (intent.destination.isDefined) {
      if (intent.targets.isEmpty &&
        intent.unit.inRadius(32 * 2).filterNot(_.isOurs).isEmpty && //Path around neutral buildings
        intent.destination.get.distanceTile(intent.unit.tileCenter) > 12) {
        return With.commander.move(intent, intent.destination.get.pixelCenter)
      }
    }
    
    val movementProfile = if (intent.targets.isEmpty) intent.normalMovement else intent.combatMovement
    
    val tile = EvaluatePositions.best(intent, movementProfile)
    return With.commander.move(intent, tile.pixelCenter)
  }
}
