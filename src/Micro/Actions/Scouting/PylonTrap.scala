package Micro.Actions.Scouting

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Basic.Build
import Micro.Agency.BuildIntent
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Strategies.Protoss.PvEProxy2Gate
import Utilities.?

object PylonTrap extends Action {

  private case class TrapPlan(target: UnitInfo, locations: Vector[PylonTrapSearch.Placement])
  private object TrapPlanKey

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    Protoss.Probe(unit)
      && PvEProxy2Gate()
      && With.microSlush.available > 0
      && unit.base.exists(m => m.owner.isEnemy && (m.owner.isTerran || m.owner.isProtoss)))

  override protected def perform(probe: FriendlyUnitInfo): Unit = {
    val existingPlan = probe.agent.get[TrapPlan](TrapPlanKey)
    val activePlan = existingPlan
      .filter(plan => plan.target.alive && ! plan.target.flying && plan.target.isEnemy)
      .flatMap(plan => {
        val remaining = plan.locations.filterNot(locationStarted)
        if (remaining.nonEmpty) Some((plan.target, remaining)) else None
      })
    if (existingPlan.nonEmpty && activePlan.isEmpty) {
      probe.agent.remove(TrapPlanKey)
    }

    val base = probe.base
    val needSupply  = With.units.ours.map(_.unitClass.supplyProvided400).sum < With.self.supplyUsed400 + 10

    if ( ! needSupply) {
      if (base.exists(b => b.isEnemy && b.isMain && b.gas.length == 1 && b.gas.forall(_.isNeutral))) {
        buildWhenReady(probe, Protoss.Assimilator, base.get.gas.head.tileTopLeft)
        return
      }
    }

    val target = activePlan.orElse({
      val newPlan = probe.metro.get.units
        .filter(u => u.isEnemy && u.isAny(Protoss.Gateway, Terran.Factory))
        .sortBy(_.frameDiscovered)
        .iterator
        .map(b => (b, PylonTrapSearch(b)))
        .collectFirst { case (building, Some(locations)) if locations.nonEmpty => (building, locations.toVector) }
      newPlan.foreach(plan => probe.agent.put(TrapPlanKey, TrapPlan(plan._1, plan._2)))
      newPlan
    })

    if (target.isDefined) {
      val locations = target.get._2
      val needPower = locations.exists(loc => ! With.grids.psi2Height.isSet(loc.tile))
      val wantPylon = needPower || needSupply
      val location  = ?(wantPylon,
        Maff.orElse(
          locations.find(x => x.canBePylon && ! x.canBeBattery),
          locations.find(x => x.canBePylon),
          locations.headOption),
        Maff.orElse(
          locations.find(x => x.canBeBattery && ! x.canBePylon),
          locations.find(x => x.canBeBattery),
          locations.headOption))
        .filter(loc => loc.canBePylon || ! needPower)

      if (location.nonEmpty) {
        val loc       = location.head
        val building  = ?(loc.canBeBattery && ( ! loc.canBePylon || ! wantPylon), Protoss.ShieldBattery, Protoss.Pylon)
        buildWhenReady(probe, building, loc.tile)
       return
      }
    }

    if (needSupply) {
      mannerPylon(probe)
    }
  }

  private def locationStarted(location: PylonTrapSearch.Placement): Boolean = {
    location.tile.units.exists(u => u.isOurs && u.unitClass.isBuilding && u.tileTopLeft == location.tile)
  }

  def mannerPylon(probe: FriendlyUnitInfo): Unit = {
    val tiles = probe.metro
      .flatMap(_.main)
      .map(_.harvestingTrafficTiles.filter(legal))
      .getOrElse(Set.empty[Tile])

    if (tiles.isEmpty) {
      anyPylon(probe)
      return
    }

    buildWhenReady(probe, Protoss.Pylon, tiles.minBy(probe.pixelDistanceSquared))
  }

  def anyPylon(probe: FriendlyUnitInfo): Unit = {
    val tile = Spiral(10)
      .map(probe.tile.add)
      .find(legal)

    if (tile.isEmpty) {
      With.logger.warn(f"${probe} needs to build a Pylon but found no location.")
      return
    }

    buildWhenReady(probe, Protoss.Pylon, tile.get)
  }

  def gasSteal(probe: FriendlyUnitInfo): Unit = {
    val base = probe.base
    if (base.exists(b => b.isEnemy && b.isMain && b.gas.length == 1 && b.gas.forall(_.isNeutral))) {
      buildWhenReady(probe, Protoss.Assimilator, base.get.gas.head.tileTopLeft)
    }
  }

  def legal(tile: Tile): Boolean = {
    val area = new TileRectangle(tile, 2, 2)
    val expanded = area.expand(1)
    With.grids.buildable22(tile) && ! expanded.tiles.exists(_.units.exists(_.tileArea.intersects(area)))
  }

  def buildWhenReady(probe: FriendlyUnitInfo, building: UnitClass, tile: Tile): Unit = {
    if (With.microSlush.available >= 76) {
      probe.intent.addBuild(BuildIntent(building, tile))
      Build.perform(probe)
      // TODO: Spending needs to resync every agent cycle before we can actually spend properly
      //With.microSlush.spend(100)
    } else {
      probe.intent.setScout(Spiral(8).map(tile.add))
    }
  }
}
