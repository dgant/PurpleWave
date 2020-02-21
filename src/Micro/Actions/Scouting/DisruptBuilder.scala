package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object DisruptBuilder extends Action {

  def scaryThreats(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.threats.view.filter(t => t.pixelsToGetInRange(unit) < 64 && ! t.constructing)
  }

  def disruptableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.enemies.filter(e => e.unitClass.isWorker && e.constructing)
  }

  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val scaries = scaryThreats(unit)
    lazy val targets = disruptableBuilders(unit)
    (unit.canAttack
     && With.intelligence.enemyMain.isDefined
     && With.enemies.exists(_.raceInitial == Race.Terran)
     && targets.nonEmpty
     && scaries.size < 2
     && (scaries.isEmpty || (unit.totalHealth > 5 && targets.exists(_.totalHealth < unit.totalHealth))))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val builders = disruptableBuilders(unit)
    val target = EvaluateTargets.best(unit, builders)
    unit.agent.toAttack = target
    Attack.delegate(unit)
  }
}
