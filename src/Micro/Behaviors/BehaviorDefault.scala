package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Movement.{EvaluatePositions, MovementProfile}
import Micro.Targeting.{EvaluateTargets, TargetingProfile, Targets}
import ProxyBwapi.Races.Protoss
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
  
    val targets = Targets.get(intent)
    
    if (intent.unit.unitClass == Protoss.Reaver
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.scarabs < (if(targets.isEmpty) 5 else 1)) {
      return With.commander.buildScarab(intent)
    }
    if (intent.unit.unitClass == Protoss.Carrier
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.interceptors < (if(targets.isEmpty) 8 else 1)) {
      return With.commander.buildInterceptor(intent)
    }
    
    if (intent.unit.cooldownRemaining < With.game.getRemainingLatencyFrames) {
      val target = EvaluateTargets.best(intent, defaultTargetProfile, targets)
      if (target.isDefined) {
        return With.commander.attack(intent, target.get)
      }
    }
    
    if (intent.destination.isDefined) {
      if (targets.isEmpty) {
        return With.commander.move(intent, intent.destination.get.pixelCenter)
      }
    }
    
    val tile = EvaluatePositions.best(intent, defaultMovementProfile)
    return With.commander.move(intent, tile.pixelCenter)
  }
  
  val defaultMovementProfile = new MovementProfile (
    preferTravel      = 0,
    preferMobility    = 2,
    preferHighGround  = 0,
    preferGrouping    = 0.1,
    avoidDamage       = 1,
    avoidTraffic      = 0.25,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val defaultTargetProfile = new TargetingProfile(
    preferInRange     = 2,
    preferValue       = 1,
    preferFocus       = 1,
    preferDps         = 2,
    avoidHealth       = 2,
    avoidDistance     = 1)
}
