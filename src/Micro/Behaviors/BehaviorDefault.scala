package Micro.Behaviors
import Micro.Intentions.Intention
import Micro.Heuristics.Movement.EvaluatePositions
import Micro.Heuristics.Targeting.EvaluateTargets
import Startup.With
import Utilities.EnrichPosition._

object BehaviorDefault extends Behavior {
  
  def execute(intent: Intention) {
    if (uninterruptible(intent)) return
    debugPauseOnSelectedUnit(intent)
    considerFleeing(intent)
    pickTarget(intent)
    attack(intent) || move(intent)
  }
  
  def debugPauseOnSelectedUnit(intent:Intention) = if (intent.unit.selected) {
    val putADebugBreakpointHere = true
  }
  
  def considerFleeing(intent:Intention) =  if (desireToFight(intent) < 1.0) intent.destination = Some(With.geography.home)
  def uninterruptible(intent:Intention):Boolean = intent.unit.attackStarting || intent.unit.attackAnimationHappening
  
  def desireToFight(intent:Intention):Double = {
    val strengthOurs  = With.grids.friendlyStrength.get(intent.unit.tileCenter)
    val strengthEnemy = With.grids.enemyStrength.get(intent.unit.tileCenter)
    val strengthRatioOverall =
      With.battles.byUnit.get(intent.unit)
        .map(battle => battle.us.strength / battle.enemy.strength)
        .getOrElse(1.0)
    
    strengthRatioOverall * strengthOurs / strengthEnemy
  }
  
  def pickTarget(intent:Intention) = intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targetProfile, intent.targets))
  
  def attack(intent:Intention):Boolean = {
    if (intent.unit.canAttackRightNow && intent.toAttack.isDefined && desireToFight(intent) > 0.5) {
      With.commander.attack(intent, intent.toAttack.get)
      return true
    }
    false
  }
  
  def move(intent:Intention):Boolean = {
    val movementProfile = if (intent.targets.isEmpty && intent.threats.isEmpty) intent.movementProfileNormal else intent.movementProfileCombat
    val tile = EvaluatePositions.best(intent, movementProfile)
    With.commander.move(intent, tile.pixelCenter)
    true
  }
}
