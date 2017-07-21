package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Pursue extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.action.canFight              &&
    unit.action.canPursue             &&
    unit.action.toAttack.isEmpty      &&
    unit.canMoveThisFrame             &&
    unit.matchups.targets.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    val pursuableTargets = unit.matchups.targets.filter(target =>
      target.topSpeed < unit.topSpeed * 0.8 ||
      (
        ! target.flying &&
        unit.pixelRangeAgainstFromCenter(target) > 32 * 3.0 &&
        target.pixelCenter.zone.edges.forall(edge =>
          edge.centerPixel.pixelDistanceFast(unit.pixelCenter) <
          edge.centerPixel.pixelDistanceFast(target.pixelCenter))
      ))
    
    unit.action.toAttack = EvaluateTargets.best(unit.action, pursuableTargets)
    if (unit.readyForAttackOrder) {
      Attack.delegate(unit)
    }
    else {
      unit.action.toAttack.foreach(target => unit.action.toTravel = Some(target.targetPixel.getOrElse(target.project(unit.cooldownLeft))))
    }
  }
}
