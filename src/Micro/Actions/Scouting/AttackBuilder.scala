package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import bwapi.Race

object AttackBuilder extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val scaries = scaryThreats(unit)
    lazy val targets = disruptableBuilders(unit)
    (unit.canAttack
      && unit.intent.canFight
      && With.scouting.enemyMainFullyScouted
      && With.enemies.exists(_.raceCurrent == Race.Terran)
      && targets.nonEmpty
      && scaries.size < 2
      && (scaries.isEmpty || (unit.totalHealth > 5 && targets.exists(_.totalHealth < unit.totalHealth))))
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack = Maff.maxBy(disruptableBuilders(unit))(t => unit.targetScore(t) * ?(t.orderTarget.exists(_.unitClass.isGas), 0.1, 1.0))
    Commander.attack(unit)
  }

  private def scaryThreats(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.threats.view.filter(t => t.pixelsToGetInRange(unit) < 64 && ! t.constructing)
  }

  private def disruptableBuilders(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.enemies.filter(e => Terran.SCV(e) && e.constructing)
  }
}
