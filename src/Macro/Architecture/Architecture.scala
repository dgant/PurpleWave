package Macro.Architecture

import Debugging.Visualizations.Views.Geography.ShowArchitecturePlacements
import Information.Geography.Types.Zone
import Information.Grids.Disposable.GridDisposableBoolean
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable

class Architecture {
  val exclusions        : mutable.ArrayBuffer[Exclusion]            = new mutable.ArrayBuffer[Exclusion]
  val unbuildable       : GridDisposableBoolean                     = new GridDisposableBoolean
  val unwalkable        : GridDisposableBoolean                     = new GridDisposableBoolean
  val ungassable        : GridDisposableBoolean                     = new GridDisposableBoolean
  val untownhallable    : GridDisposableBoolean                     = new GridDisposableBoolean
  val creep             : GridDisposableBoolean                     = new GridDisposableBoolean
  val powered2Height    : GridDisposableBoolean                     = new GridDisposableBoolean
  val powered3Height    : GridDisposableBoolean                     = new GridDisposableBoolean
  var accessibleZones   : Vector[Zone]                              = Vector.empty
    
  def usuallyNeedsMargin(unitClass: UnitClass): Boolean = {
    if (With.configuration.enableTightBuildingPlacement) {
      unitClass.isBuilding &&
      unitClass.trainsGroundUnits &&
      ! unitClass.isTownHall //Nexus margins bork FFEs. Down the road Hatcheries may need margins.
    }
    else true
  }
  
  def reboot() {
    exclusions      .clear()
    unbuildable     .update()
    unwalkable      .update()
    ungassable      .update()
    untownhallable  .update()
    powered2Height  .update()
    powered3Height  .update()
    recalculateExclusions()
    recalculatePower()
    recalculateBuilderAccess()
  }
  
  def buildable(tile: Tile): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.get(tile)
  }
  
  def walkable(tile: Tile): Boolean = {
    With.grids.walkable.get(tile) &&
      ! unwalkable.get(tile) &&
      ! tile.zone.bases.exists(_.townHallArea.contains(tile))
  }
  
  def assumePlacement(placement: Placement) {
    if (placement.tile.isEmpty) return
    
    val tile = placement.tile.get
  
    val area = TileRectangle(
      tile.add(placement.blueprint.relativeBuildStart),
      tile.add(placement.blueprint.relativeBuildEnd))

    val nTiles = area.tiles.size
    var iTile = 0
    while (iTile < nTiles) {
      val tile = area.tiles(iTile)
      unbuildable.set(tile, true)
      unwalkable.set(tile, true)
      untownhallable.set(tile, true)
      ungassable.set(tile, true)
      iTile += 1
    }

    // If we have no Pylons, place in advance of our first completing
    if (placement.blueprint.powers.get && ! With.units.existsOurs(Protoss.Pylon)) {
      addPower(tile)
    }
    
    if (ShowArchitecturePlacements.inUse) {
      exclusions += Exclusion(placement.blueprint.toString, area)
    }
  }
  
  /////////////
  // Margins //
  /////////////

  private def recalculateExclusions() {

    // Reserve addon space in bases
    if (With.self.isTerran) With.geography.bases.map(base => {
      val start = base.townHallTile.add(4, 1)
      val addonArea = TileRectangle(start, start.add(2, 2))
      addonArea.tiles.foreach(unbuildable.set(_, true))
      if (ShowArchitecturePlacements.inUse) {
        exclusions += Exclusion("Addon for " + base, addonArea)
      }
    })

    With.units.ours.foreach(unit => {

      // Reserve unit's addon space
      if (unit.unitClass.canBuildAddon) {
        unit.addonArea.tiles.foreach(unbuildable.set(_, true))
        if (ShowArchitecturePlacements.inUse) {
          exclusions += Exclusion("Addon for " + unit, unit.addonArea)
        }
      }

      // Reserve margins for buildings which produce ground units
      val expandMargin =
      if ( ! unit.flying && (unit.isAny(Zerg.Egg, Zerg.LurkerEgg) || (unit.unitClass.isBuilding && usuallyNeedsMargin(unit.unitClass)))) {
        val w = 1 + unit.unitClass.tileWidth
        val h = 1 + unit.unitClass.tileHeight
        var x = 0
        while(x < w+1) {
          unwalkable.set(unit.tileTopLeft.add(x-1, -1), true)
          unwalkable.set(unit.tileTopLeft.add(x-1, h-1), true)
          x += 1
        }
        var y = 0
        while(y < h+1) {
          unwalkable.set(unit.tileTopLeft.add(-1,  y-1), true)
          unwalkable.set(unit.tileTopLeft.add(w-1, y-1), true)
          y += 1
        }
        if (ShowArchitecturePlacements.inUse) {
          exclusions += Exclusion("Margin for " + unit, unit.tileArea.expand(1, 1))
        }
      }
    })

    // Flag places where we can't build gas
    With.geography.bases.foreach(_.gas.foreach(gas =>
      if (gas.alive && ! gas.player.isNeutral && gas.alive) {
        ungassable.set(gas.tileTopLeft, true)
      }
    ))
  }
  
  ///////////
  // Power //
  ///////////
  
  private def recalculatePower() {
    With.units.ours.foreach(unit =>
      if (unit.is(Protoss.Pylon)
        && (With.framesSince(unit.completionFrame) < GameTime(0, 3)()
          || unit.zone.units.forall(other => ! other.is(Protoss.Pylon) || other.completionFrame >= unit.completionFrame))) {
        addPower(unit.tileTopLeft)
      })
  }
  
  private def addPower(tile: Tile) {
    With.grids.psi2Height.psiPoints.map(tile.add).foreach(neighbor => if (neighbor.valid) powered2Height.set(neighbor, true))
    With.grids.psi3Height.psiPoints.map(tile.add).foreach(neighbor => if (neighbor.valid) powered3Height.set(neighbor, true))
  }

  private def recalculateBuilderAccess() {
    val hasBuilder = With.geography.zones.filter(_.units.exists(u => u.isOurs && u.unitClass.isWorker))
    val accessible = With.geography.zones.filter(z => hasBuilder.exists(_.distancePixels(z) < Int.MaxValue))
    accessibleZones = (hasBuilder ++ accessible).distinct
  }
}
