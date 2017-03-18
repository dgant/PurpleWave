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
      if (intent.targets.isEmpty && intent.destination.get.distanceTile(intent.unit.tileCenter) > 12) {
        return With.commander.move(intent, intent.destination.get.pixelCenter)
      }
    }
    
    val movementProfile = if (intent.targets.isEmpty) normalMovement else combatMovement
    
    val tile = EvaluatePositions.best(intent, movementProfile)
    return With.commander.move(intent, tile.pixelCenter)
  }
  
  val normalMovement = new MovementProfile (
    preferTravel      = 1,
    preferSpot        = 0.25,
    preferMobility    = 0,
    preferHighGround  = 0.5,
    preferGrouping    = 0.1,
    preferRandom      = 0.25,
    avoidDamage       = 0,
    avoidTraffic      = 0.75,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val combatMovement = new MovementProfile (
    preferTravel      = 0.25,
    preferSpot        = 0,
    preferMobility    = 2,
    preferHighGround  = 1,
    preferGrouping    = 1,
    preferRandom      = 0.1,
    avoidDamage       = 2,
    avoidTraffic      = 1,
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
