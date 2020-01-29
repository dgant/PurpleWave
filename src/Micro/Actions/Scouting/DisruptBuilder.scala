package Micro.Actions.Scouting

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

object DisruptBuilder extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val targets = disruptableBuilders(unit)
    (unit.canAttack
     && With.intelligence.enemyMain.isDefined
     && With.enemies.exists(_.raceInitial == Race.Terran)
     && targets.nonEmpty
     && (unit.matchups.threatsViolent.isEmpty || targets.exists(canKillInTime(unit, _))))
  }
  
  protected def canKillInTime(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    val framesToKill = unit.cooldownLeft + (target.hitPoints / unit.dpfOnNextHitAgainst(target))
    val framesToLive = unit.hitPoints / unit.matchups.threatsViolent.map(t => t.dpfOnNextHitAgainst(unit)).sum
    framesToKill <= framesToLive
  }
  
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
