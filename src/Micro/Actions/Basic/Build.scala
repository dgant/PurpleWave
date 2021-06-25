package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Targeting.Target
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{CircularPush, TrafficPriorities}
import Planning.UnitMatchers.MatchWorker
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo


object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.toBuild.isDefined && unit.intent.toBuildTile.isDefined
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val ourBuilding = With.grids.units.get(unit.intent.toBuildTile.get).find(_.unitClass == unit.intent.toBuild.get)
    
    if (ourBuilding.isDefined) {
      unit.agent.toGather = Maff.minBy(With.geography.ourBases.flatMap(_.minerals))(_.pixelDistanceCenter(unit.pixel))
      Gather.consider(unit)
      return
    }
    
    val distance  = unit.pixelDistanceCenter(unit.intent.toBuildTile.get.center)
    val buildArea = unit.intent.toBuild.get.tileArea.add(unit.intent.toBuildTile.get)
    
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
      lazy val noThreats  = unit.matchups.threats.isEmpty
      lazy val allWorkers = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      lazy val healthy    = unit.totalHealth > 10 || unit.totalHealth >= unit.matchups.threats.head.totalHealth
      if (noThreats || (allWorkers && healthy) && unit.cooldownLeft < With.reaction.agencyMax) {
        Target.choose(unit)
        unit.agent.toAttack = unit.agent.toAttack.orElse(Some(blockersToKill.minBy(_.pixelDistanceEdge(unit))))
        Commander.attack(unit)
      }
      else if (unit.matchups.threats.exists( ! _.is(MatchWorker))) {
        FightOrFlight.consider(unit)
        Fight.consider(unit)
      }
    }
    
    if (unit.unready) return
    
    val buildClass = unit.intent.toBuild.get
    val buildTile = unit.intent.toBuildTile.get

    val pushPixel = buildArea.midPixel
    val priority = if (unit.pixelDistanceCenter(pushPixel) < 128) TrafficPriorities.Shove else TrafficPriorities.Bump
    With.coordinator.pushes.put(new CircularPush(priority, pushPixel, 32 + buildClass.dimensionMax, unit))

    if (unit.tile.tileDistanceFast(buildTile) < 5 && buildTile.visible) {
      Commander.build(unit, buildClass, buildTile)
      return
    }

    var movePixel = buildTile.topLeftPixel
    if (unit.is(Zerg.Drone)) {
      movePixel = movePixel.add(buildClass.width / 2, buildClass.height / 2)
    }
    unit.agent.toTravel = Some(movePixel)
    MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    Commander.move(unit)
  }
}
