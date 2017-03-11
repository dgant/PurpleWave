package Global.Combat.Commands
import Global.Combat.Movement.{EvaluatePositions, MovementProfile}
import Global.Combat.Targeting.{EvaluateTargets, TargetProfile, Targets}
import Startup.With
import Types.Intents.Intention
import Utilities.Enrichment.EnrichPosition._

object Control extends Command {
  
  def execute(intent: Intention) {
    
    if (intent.unit.cooldownRemaining < With.game.getRemainingLatencyFrames) {
      val targets = Targets.get(intent)
      val target = EvaluateTargets.best(intent, defaultTargetProfile, targets)
      if (target.isDefined) {
        With.commander.attack(intent.unit, target.get)
        return
      }
    }
    val tile = EvaluatePositions.best(intent, defaultMovementProfile)
    With.commander.move(intent.unit, tile.centerPixel)
  }
  
  val defaultMovementProfile = new MovementProfile(
    preferTravel      = 1,
    preferMobility    = 1,
    preferHighGround  = 1,
    preferGrouping    = 1,
    avoidDamage       = 1,
    avoidTraffic      = 1,
    avoidVision       = 0,
    avoidDetection    = 0
  )
  
  val defaultTargetProfile = new TargetProfile(
    preferInRange     = 1,
    preferValue       = 1,
    preferFocus       = 1,
    avoidHealth       = 1,
    avoidDistance     = 1)
}
