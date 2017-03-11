package Global.Combat.Commands
import Global.Combat.Movement.{EvaluatePositions, MovementProfile}
import Global.Combat.Targeting.{EvaluateTargets, TargetProfile, Targets}
import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._

object Control extends Command {
  
  def execute(intent: Intention) {
  
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
    preferTravel      = 1,
    preferMobility    = 0.75,
    preferHighGround  = 0.5,
    preferGrouping    = 0.5,
    avoidDamage       = 2,
    avoidTraffic      = 1,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val defaultTargetProfile = new TargetProfile(
    preferInRange     = 3,
    preferValue       = 2,
    preferFocus       = 2,
    avoidHealth       = 2,
    avoidDistance     = 1)
}
