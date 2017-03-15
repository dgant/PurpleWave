package Global.Combat.Behaviors
import Global.Combat.Movement.{EvaluatePositions, MovementProfile}
import Global.Combat.Targeting.{EvaluateTargets, TargetProfile, Targets}
import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._
import bwapi.UnitType

object DefaultBehavior extends Behavior {
  
  def execute(intent: Intention) {
  
    if (intent.unit.utype == UnitType.Protoss_Reaver
      && intent.unit.trainingQueue.isEmpty
      && intent.unit.scarabs < 5) {
      With.commander.buildScarab(intent.unit)
      return
    }
    if (intent.unit.utype == UnitType.Protoss_Carrier
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
  
  val defaultTargetProfile = new TargetProfile(
    preferInRange     = 2,
    preferValue       = 1,
    preferFocus       = 1,
    preferDps         = 2,
    avoidHealth       = 2,
    avoidDistance     = 1)
}
