package Micro.Behaviors
import Micro.Intent.Intention
import Micro.Heuristics.Movement.EvaluatePositions
import Micro.Heuristics.Targeting.EvaluateTargets
import Lifecycle.With
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
    With.battles.byUnit.get(intent.unit)
      .map(battle => (1.0 + battle.us.strength) / (1.0 + battle.enemy.strength))
      .getOrElse(1.0)
  }
  
  def pickTarget(intent:Intention) = intent.toAttack = intent.toAttack.orElse(EvaluateTargets.best(intent, intent.targetProfile, intent.targets))
  
  def attack(intent:Intention):Boolean = {
    val willingToFight = desireToFight(intent) > 0.5
    if (
      intent.unit.canAttackThisFrame &&
      intent.toAttack.isDefined &&
        (willingToFight ||
          (intent.toAttack.exists(intent.unit.inRangeToAttack)
          && ! intent.unit.flying))) {
      With.commander.attack(intent, intent.toAttack.get)
      return true
    }
    
    if ( ! willingToFight) intent.toAttack = None
    false
  }
  
  def move(intent:Intention):Boolean = {
    val tileToMove =
      if (intent.threats.nonEmpty || intent.targets.nonEmpty)
        EvaluatePositions.best(intent, intent.movementProfileCombat, 3)
      else if (With.configuration.enableHeuristicTravel)
        EvaluatePositions.best(intent, intent.movementProfileNormal, 2)
      else {
        
        intent.destination.getOrElse(intent.unit.tileCenter)
      }
        
    
    With.commander.move(intent, tileToMove.pixelCenter)
    true
  }
}
