package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object DisruptBuilder extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.enemies.exists(_.race == Race.Terran) &&
    disruptableBuilders(unit).nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builders = disruptableBuilders(unit)
    
    if (unit.matchups.threatsViolent.isEmpty) {
      val target = EvaluateTargets.best(unit.action, builders)
      unit.action.toAttack = target
      Attack.delegate(unit)
    }
  }
  
  def disruptableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.enemies.filter(_.constructing)
  }
}
