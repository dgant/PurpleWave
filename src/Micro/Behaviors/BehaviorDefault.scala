package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Movement.{EvaluatePositions, MovementProfile}
import Micro.Targeting.{EvaluateTargets, TargetingProfile}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
    
    if (intent.unit.cooldownRemaining < With.game.getRemainingLatencyFrames) {
      val target = EvaluateTargets.best(intent, defaultTargetProfile, intent.targets)
      if (target.isDefined) {
        return With.commander.attack(intent, target.get)
      }
    }
    
    if (intent.destination.isDefined) {
      if (intent.targets.isEmpty) {
        return With.commander.move(intent, intent.destination.get.pixelCenter)
      }
    }
    
    val tile = EvaluatePositions.best(intent, defaultMovementProfile)
    return With.commander.move(intent, tile.pixelCenter)
  }
  
  val defaultMovementProfile = new MovementProfile (
    preferTravel      = 0,
    preferMobility    = 1,
    preferHighGround  = 0,
    preferGrouping    = 0,
    avoidDamage       = 1,
    avoidTraffic      = 0,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val defaultTargetProfile = new TargetingProfile(
    preferInRange     = 2,
    preferValue       = 1,
    preferFocus       = 1,
    preferDps         = 2,
    avoidHealth       = 3,
    avoidDistance     = 1)
}
