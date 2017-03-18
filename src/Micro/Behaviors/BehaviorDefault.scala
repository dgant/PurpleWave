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
    preferSpot        = 0.2,
    preferMobility    = 0.25,
    preferHighGround  = 0.5,
    preferGrouping    = 0.25,
    preferRandom      = 0.15,
    avoidDamage       = 0,
    avoidTraffic      = 1,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val combatMovement = new MovementProfile (
    preferTravel      = 0.2,
    preferSpot        = 0,
    preferSitAtRange  = 1,
    preferMobility    = 2,
    preferHighGround  = 0.5,
    preferGrouping    = 0,
    preferRandom      = 0,
    avoidDamage       = 3,
    avoidTraffic      = 0.5,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val defaultTargetProfile = new TargetingProfile(
    preferInRange     = 0.5,
    preferValue       = 0.2,
    preferFocus       = 0.2,
    preferDps         = 3,
    avoidHealth       = 1,
    avoidDistance     = 0.2)
}
