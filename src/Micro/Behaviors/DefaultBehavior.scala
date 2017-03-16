package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Movement.{EvaluatePositions, MovementProfile}
import Micro.Targeting.{EvaluateTargets, TargetingProfile, Targets}
import ProxyBwapi.UnitClass.{Carrier, Reaver}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object DefaultBehavior extends Behavior {
  
  def execute(intent: Intention) {
  
    if (intent.unit.utype == Reaver
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.scarabs < 5) {
      With.commander.buildScarab(intent.unit)
      return
    }
    if (intent.unit.utype == Carrier
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.interceptors < 8) {
      With.commander.buildInterceptor(intent.unit)
      return
    }
  
    val targets = Targets.get(intent)
    
    if (intent.unit.cooldownRemaining < With.game.getRemainingLatencyFrames) {
      val target = EvaluateTargets.best(intent, defaultTargetProfile, targets)
      if (target.isDefined) {
        With.commander.attack(intent.unit, target.get)
        return
      }
    }
    
    if (targets.isEmpty) {
      With.commander.move(intent.unit, intent.destination.centerPixel)
    }
    else {
      val tile = EvaluatePositions.best(intent, defaultMovementProfile)
      With.commander.move(intent.unit, tile.centerPixel)
    }
  }
  
  val defaultMovementProfile = new MovementProfile(
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
