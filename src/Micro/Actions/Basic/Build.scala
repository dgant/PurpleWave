package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Actions.Commands.Attack
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBuild.isDefined &&
    unit.agent.toBuildTile.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val distance  = unit.pixelDistanceFast(unit.agent.toBuildTile.get.pixelCenter)
    val buildArea = unit.agent.toBuild.get.tileArea.add(unit.agent.toBuildTile.get)
    val blockers  = if (distance > 32.0 * 8.0) Seq.empty else
      buildArea
        .expand(2, 2)
        .tiles
        .flatMap(With.grids.units.get(_).filter(blocker =>
          blocker != unit   &&
          ! blocker.flying  &&
          blocker.possiblyStillThere))
        .toSeq
    
    blockers.flatMap(_.friendly).foreach(_.agent.shove(unit))
    val enemyBlockers = blockers.filter(_.isEnemy)
    if (enemyBlockers.nonEmpty) {
      unit.agent.canFight = true
      val noThreats   = unit.matchups.threats.isEmpty
      val allWorkers  = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      if (noThreats || (allWorkers && (unit.totalHealth > 10 || unit.totalHealth > unit.matchups.threats.head.totalHealth))) {
        unit.agent.toAttack = Some(enemyBlockers.minBy(_.pixelDistanceFast(unit)))
        Attack.delegate(unit)
      }
      else {
        FightOrFlight.consider(unit)
        Fight.consider(unit)
      }
    }
    else {
      With.commander.build(unit, unit.agent.toBuild.get, unit.agent.lastIntent.toBuildTile.get)
    }
  }
}
