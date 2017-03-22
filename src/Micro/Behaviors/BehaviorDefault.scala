package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Movement.EvaluatePositions
import Micro.Targeting.EvaluateTargets
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
  
    if (intent.unit.selected) {
      val putADebugBreakpointHere = true
    }
    
    if (intent.unit.attackStarting || intent.unit.attackAnimationHappening) {
      return
    }
    
    val desireToFight = getDesireToFight(intent)
    
    if (desireToFight < 1.0) {
      intent.destination = Some(With.geography.home)
    }
    
    if (desireToFight > 0.75) {
      val target = EvaluateTargets.best(intent, intent.targetProfile, intent.targets)
  
      if (intent.unit.cooldownLeft < With.game.getRemainingLatencyFrames) {
        if (target.isDefined) {
          return With.commander.attack(intent, target.get)
        }
      }
    }
    
    if (intent.destination.isDefined) {
      if (intent.targets.isEmpty &&
        intent.unit.inPixelRadius(32 * 2).filterNot(_.isOurs).isEmpty && //Path around neutral buildings
        intent.destination.get.distanceTile(intent.unit.tileCenter) > 12) {
        return With.commander.move(intent, intent.destination.get.pixelCenter)
      }
    }
    
    val movementProfile = if (intent.targets.isEmpty) intent.movementProfileNormal else intent.movementProfileCombat
    
    val tile = EvaluatePositions.best(intent, movementProfile)
    return With.commander.move(intent, tile.pixelCenter)
  }
  
  def getDesireToFight(intent:Intention):Double = {
    val strengthOurs  = With.grids.friendlyStrength.get(intent.unit.tileCenter)
    val strengthEnemy = With.grids.enemyStrength.get(intent.unit.tileCenter)
    val strengthRatioOverall =
      With.battles.byUnit.get(intent.unit)
        .map(battle => battle.us.strength / battle.enemy.strength)
        .getOrElse(1.0)
    
    return strengthRatioOverall * strengthOurs / strengthEnemy
  }
}
