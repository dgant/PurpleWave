package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Actions.Combat.Targeting.Target
import Micro.Coordination.Pushing.{CircularPush, TrafficPriorities}
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBuild.isDefined &&
    unit.agent.toBuildTile.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val ourBuilding = With.grids.units.get(unit.agent.toBuildTile.get).find(_.unitClass == unit.agent.toBuild.get)
    
    if (ourBuilding.isDefined) {
      unit.agent.toGather = ByOption.minBy(With.geography.ourBases.flatMap(_.minerals))(_.pixelDistanceCenter(unit.pixelCenter))
      Gather.consider(unit)
      return
    }
    
    val distance  = unit.pixelDistanceCenter(unit.agent.toBuildTile.get.pixelCenter)
    val buildArea = unit.agent.toBuild.get.tileArea.add(unit.agent.toBuildTile.get)
    
    def blockersForTile(tile: Tile) = {
      With.grids.units
        .get(tile)
        .filter(blocker =>
          blocker != unit
          && ! blocker.unitClass.isGas
          && ! blocker.flying
          && blocker.likelyStillThere)
    }
    
    val ignoreBlockers        = distance > 32.0 * 8.0 || With.yolo.active()
    lazy val blockersIn       = if (ignoreBlockers) Seq.empty else buildArea.tiles.flatMap(blockersForTile).toSeq
    lazy val blockersNear     = if (ignoreBlockers) Seq.empty else buildArea.expand(2, 2).tiles.flatMap(blockersForTile).toSeq
    lazy val blockersOurs     = blockersNear.filter(_.isOurs)
    lazy val blockersEnemy    = blockersNear.filter(_.isEnemy)
    lazy val blockersMineral  = blockersIn.filter(_.unitClass.isMinerals)
    lazy val blockersNeutral  = blockersIn.filter(blocker => blocker.isNeutral && ! blockersMineral.contains(blocker)).filterNot(_.invincible)
    lazy val blockersToKill   = (if (blockersEnemy.nonEmpty) blockersEnemy else blockersNeutral).filter(unit.canAttack)
    
    if (blockersMineral.nonEmpty && blockersEnemy.isEmpty) {
      unit.agent.toGather = Some(blockersMineral.head)
      Gather.delegate(unit)
    }
    else if(blockersToKill.nonEmpty) {
      unit.agent.canFight = true
      lazy val noThreats  = unit.matchups.threats.isEmpty
      lazy val allWorkers = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      lazy val healthy    = unit.totalHealth > 10 || unit.totalHealth >= unit.matchups.threats.head.totalHealth
      if (noThreats || (allWorkers && healthy) && unit.cooldownLeft < With.reaction.agencyMax) {
        Target.delegate(unit)
        unit.agent.toAttack = unit.agent.toAttack.orElse(Some(blockersToKill.minBy(_.pixelDistanceEdge(unit))))
        With.commander.attack(unit)
      }
      else if (unit.matchups.threats.exists( ! _.is(UnitMatchWorkers))) {
        FightOrFlight.consider(unit)
        Fight.consider(unit)
      }
    }
    
    if (unit.unready) return
    
    val buildClass = unit.agent.toBuild.get
    val buildTile = unit.agent.lastIntent.toBuildTile.get

    With.coordinator.pushes.put(new CircularPush(TrafficPriorities.Shove, buildArea.midPixel, 32 + buildClass.dimensionMax))

    if (unit.tileIncludingCenter.tileDistanceFast(buildTile) < 5 && With.grids.friendlyVision.isSet(buildTile)) {
      With.commander.build(unit, buildClass, buildTile)
      return
    }

    var movePixel = buildTile.topLeftPixel
    if (unit.is(Zerg.Drone)) {
      movePixel = movePixel.add(buildClass.width / 2, buildClass.height / 2)
    }

    With.commander.move(unit)
  }
}
