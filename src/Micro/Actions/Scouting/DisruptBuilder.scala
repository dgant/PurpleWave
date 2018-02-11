package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object DisruptBuilder extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canAttack
    && With.intelligence.enemyMain.isDefined
    && With.enemies.exists(_.raceInitial == Race.Terran)
    && disruptableBuilders(unit).nonEmpty
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builders = disruptableBuilders(unit)
    val target = EvaluateTargets.best(unit, builders)
    unit.agent.toAttack = target
    Attack.delegate(unit)
  }
  
  def disruptableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.enemies.filter(e => e.unitClass.isWorker && e.constructing)
  }
}
