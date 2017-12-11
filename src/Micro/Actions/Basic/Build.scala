package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
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
    
    def blockersForTile(tile: Tile) = {
      With.grids.units
        .get(tile)
        .filterNot(_.unitClass.isGas)
        .filter(blocker =>
          blocker != unit   &&
            ! blocker.flying  &&
            blocker.possiblyStillThere)
    }
    
    val ignoreBlockers = distance > 32.0 * 8.0
    lazy val blockersIn       = if (ignoreBlockers) Seq.empty else buildArea.tiles.flatMap(blockersForTile).toSeq
    lazy val blockersNear     = if (ignoreBlockers) Seq.empty else buildArea.expand(2, 2).tiles.flatMap(blockersForTile).toSeq
    lazy val blockersOurs     = blockersNear.filter(_.isOurs)
    lazy val blockersEnemy    = blockersNear.filter(_.isEnemy)
    lazy val blockersMineral  = blockersIn.filter(_.unitClass.isMinerals)
    lazy val blockersNeutral  = blockersIn.filter(blocker => blocker.isNeutral && ! blockersMineral.contains(blocker)).filterNot(_.invincible)
    lazy val blockersToKill   = if (blockersEnemy.nonEmpty) blockersEnemy else blockersNeutral
    
    if (blockersMineral.nonEmpty && blockersEnemy.isEmpty) {
      unit.agent.toGather = Some(blockersMineral.head)
      Gather.delegate(unit)
    }
    else if(blockersToKill.nonEmpty) {
      unit.agent.canFight = true
      lazy val noThreats  = unit.matchups.threats.isEmpty
      lazy val allWorkers = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      lazy val healthy    = unit.totalHealth > 10 || unit.totalHealth >= unit.matchups.threats.head.totalHealth
      if (noThreats || (allWorkers && healthy)) {
        Target.delegate(unit)
        unit.agent.toAttack = unit.agent.toAttack.orElse(Some(blockersToKill.minBy(_.pixelDistanceFast(unit))))
        Attack.delegate(unit)
      }
      else {
        FightOrFlight.consider(unit)
        Fight.consider(unit)
      }
    }
    else {
      blockersOurs.flatMap(_.friendly).foreach(_.agent.shove(unit))
      With.commander.build(unit, unit.agent.toBuild.get, unit.agent.lastIntent.toBuildTile.get)
    }
  }
}
