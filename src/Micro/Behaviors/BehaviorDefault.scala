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
  
  def considerFleeing(intent:Intention) = if (desireToFight(intent) < 1.0) intent.destination = Some(With.geography.home)
  def uninterruptible(intent:Intention):Boolean = intent.unit.attackStarting || intent.unit.attackAnimationHappening
  
  def desireToFight(intent:Intention):Double = {
    val strengthOurs  = 1.0 + With.grids.friendlyStrength.get(intent.unit.tileCenter)
    val strengthEnemy = 1.0 + With.grids.enemyStrength.get(intent.unit.tileCenter)
    val strengthRatioOverall =
      With.battles.byUnit.get(intent.unit)
        .map(battle => (1.0 + battle.us.strength) / (1.0 + battle.enemy.strength))
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
    val tileToMove =
      if (intent.threats.nonEmpty || intent.targets.nonEmpty)
        EvaluatePositions.best(intent, intent.movementProfileCombat)
      else if (With.configuration.enableHeuristicTravel)
        EvaluatePositions.best(intent, intent.movementProfileNormal)
      else
        intent.destination.getOrElse(intent.unit.tileCenter)
    
    With.commander.move(intent, tileToMove.pixelCenter)
    true
  }
}
