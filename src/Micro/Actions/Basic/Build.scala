package Micro.Actions.Basic

import Debugging.Visualizations.Forces
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.FightOrFlee
import Micro.Actions.Combat.Fight
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{CircularPush, TrafficPriorities}
import Micro.Heuristics.Potential
import Micro.Targeting.Target
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.UnitFilters.IsWorker

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.intent.toBuild.nonEmpty
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    val intent      = unit.intent.toBuildActive.get
    val building    = intent.unitClass
    val tile        = intent.tile

    // If building already started, get a head start on mining
    if (tile.units.exists(u => u.isOurs && u.unitClass.isBuilding && (u.complete || u.unitClass.isProtoss))) {
      if (unit.intent.toScoutTiles.isEmpty) {
        unit.agent.toGather = Maff.minBy(With.geography.ourBases.flatMap(_.minerals))(_.pixelDistanceCenter(unit.pixel))
        Gather(unit)
      }
      return
    }
    
    def blockersForTile(tile: Tile) = tile.units.filter(blocker =>
      blocker != unit
      && ! blocker.unitClass.isGas
      && ! blocker.flying
      && blocker.visible)

    val buildArea             = building.tileArea.add(tile)
    val ignoreBlockers        = unit.pixelDistanceTravelling(buildArea.center) > 32.0 * 8.0 || With.yolo.active
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

    val pushPixel = buildArea.center
    Commander.defaultEscalation(unit)
    val priority = if (unit.pixelDistanceCenter(pushPixel) < 128) TrafficPriorities.Shove else TrafficPriorities.Bump

    With.coordinator.pushes.put(new CircularPush(unit.agent.priority, pushPixel, 32 + building.radialHypotenuse, unit))

    var movePixel = tile.topLeftPixel.add(building.tileWidth * 16, building.tileHeight * 16)

    if (intent.startNow
      && tile.visible
      && unit.pixelDistanceCenter(movePixel) < 112 // Addtl reference: McRave uses 96
      && With.self.minerals >= building.mineralPrice - 8
      && With.self.gas      >= building.gasPrice - 8) {
      Commander.build(unit, building, tile)
      return
    }

    if (Protoss.Probe(unit)) {
      // If unit doesn't have to resolve collision after moving it gets back to work faster
      movePixel = movePixel.add(
        if (unit.x < movePixel.x) - unit.unitClass.dimensionRightInclusive - building.dimensionLeft - 1 else unit.unitClass.dimensionLeft + building.dimensionRightInclusive + 1,
        if (unit.y < movePixel.y) - unit.unitClass.dimensionDownInclusive  - building.dimensionUp   - 1 else unit.unitClass.dimensionUp   + building.dimensionDownInclusive + 1).walkablePixel
    } else if (Zerg.Drone(unit)) {
      // McRave found that positioning Drone (0, -7) from building location minimizes wiggling
      movePixel = movePixel.add(building.tileWidth * 16, building.tileHeight * 16 - 7)
    }

    unit.agent.decision.set(movePixel)
    if (unit.pixelDistanceTravelling(movePixel) > 256 + ?(unit.agent.path.isEmpty, 0, 256)) {
      MicroPathing.tryMovingAlongTilePath(unit, MicroPathing.getSneakyPath(unit))
    }

    if ( ! intent.startNow && unit.matchups.pixelsEntangled > -64) {
      Potential.hardAvoidThreatRange(unit, 64)
      unit.agent.forces(Forces.travel) = Potential.towards(unit, movePixel) * 0.5
    }

    Commander.move(unit)
  }
}
