package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.FightOrFlee
import Micro.Actions.Combat.Fight
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{CircularPush, TrafficPriorities}
import Micro.Targeting.Target
import Utilities.UnitFilters.IsWorker
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.toBuild.isDefined && unit.intent.toBuildTile.isDefined
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    
    val ourBuilding = unit.intent.toBuildTile.get.units.find(_.unitClass == unit.intent.toBuild.get)
    
    if (ourBuilding.exists(u => u.complete || u.unitClass.isProtoss)) {
      unit.agent.toGather = Maff.minBy(With.geography.ourBases.flatMap(_.minerals))(_.pixelDistanceCenter(unit.pixel))
      Gather(unit)
      return
    }
    
    val distance  = unit.pixelDistanceCenter(unit.intent.toBuildTile.get.center)
    val buildArea = unit.intent.toBuild.get.tileArea.add(unit.intent.toBuildTile.get)
    
    def blockersForTile(tile: Tile) = tile.units.filter(blocker =>
      blocker != unit
      && ! blocker.unitClass.isGas
      && ! blocker.flying
      && blocker.visible)
    
    val ignoreBlockers        = distance > 32.0 * 8.0 || With.yolo.active
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
    } else if(blockersToKill.nonEmpty) {
      lazy val noThreats  = unit.matchups.threats.isEmpty
      lazy val allWorkers = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      lazy val healthy    = unit.totalHealth > 10 || unit.totalHealth >= unit.matchups.threats.head.totalHealth
      if (noThreats || (allWorkers && healthy) && unit.cooldownLeft < With.reaction.agencyMax) {
        Target.choose(unit)
        unit.agent.toAttack = unit.agent.toAttack.orElse(Some(blockersToKill.minBy(_.pixelDistanceEdge(unit))))
        Commander.attack(unit)
      } else if (unit.matchups.threats.exists( ! _.is(IsWorker))) {
        FightOrFlee(unit)
        Fight(unit)
      }
    }
    
    if (unit.unready) return
    
    val buildClass = unit.intent.toBuild.get
    val buildTile = unit.intent.toBuildTile.get

    val pushPixel = buildArea.center
    Commander.defaultEscalation(unit)
    val priority = if (unit.pixelDistanceCenter(pushPixel) < 128) TrafficPriorities.Shove else TrafficPriorities.Bump
    With.coordinator.pushes.put(new CircularPush(unit.agent.priority, pushPixel, 32 + buildClass.radialHypotenuse, unit))

    var movePixel = buildTile.topLeftPixel.add(buildClass.tileWidth * 16, buildClass.tileHeight * 16)

    if (buildTile.visible
        && unit.pixelDistanceCenter(movePixel) < 112 // Addtl reference: McRave uses 96
        && With.self.minerals >= buildClass.mineralPrice - 8
        && With.self.gas >= buildClass.gasPrice - 8) {
      Commander.build(unit, buildClass, buildTile)
      return
    }

    if (Protoss.Probe(unit)) {
      // If unit doesn't have to resolve collision after moving it gets back to work faster
      movePixel = movePixel.add(
        if (unit.x < movePixel.x) - unit.unitClass.dimensionRight - buildClass.dimensionLeft - 1 else unit.unitClass.dimensionLeft + buildClass.dimensionRight + 1,
        if (unit.y < movePixel.y) - unit.unitClass.dimensionDown  - buildClass.dimensionUp   - 1 else unit.unitClass.dimensionUp   + buildClass.dimensionDown + 1).walkablePixel
    } else if (Zerg.Drone(unit)) {
      // McRave found that positioning Drone (0, -7) from building location minimizes wiggling
      movePixel = movePixel.add(buildClass.tileWidth * 16, buildClass.tileHeight * 16 - 7)
    }

    unit.agent.decision.set(movePixel)
    if (unit.pixelDistanceTravelling(movePixel) > 256 + ?(unit.agent.path.isEmpty, 0, 256)) {
      MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    }
    Commander.move(unit)
  }
}
