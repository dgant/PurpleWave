package Macro.Architecture

import Information.Geography.Types.Zone
import Information.Grids.Disposable.GridDisposableBoolean
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

class Architecture {
  val unbuildable     : GridExclusion = new GridExclusion
  val unwalkable      : GridExclusion = new GridExclusion
  val ungassable      : GridExclusion = new GridExclusion
  val untownhallable  : GridExclusion = new GridExclusion
  val creep           : GridExclusion = new GridExclusion
  val powered2Height  : GridDisposableBoolean = new GridDisposableBoolean
  val powered3Height  : GridDisposableBoolean = new GridDisposableBoolean
  var accessibleZones : Vector[Zone]  = Vector.empty

  def usuallyNeedsMargin(unitClass: UnitClass): Boolean = {
    if (With.configuration.enableTightBuildingPlacement) {
      unitClass.isBuilding &&
      unitClass.trainsGroundUnits &&
      ! unitClass.isTownHall //Nexus margins bork FFEs. Down the road Hatcheries may need margins.
    }
    else true
  }

  def reboot() {
    unbuildable   .update()
    unwalkable    .update()
    ungassable    .update()
    untownhallable.update()
    powered2Height.update()
    powered3Height.update()
    recalculateExclusions()
    recalculatePower()
    recalculateBuilderAccess()
  }

  def buildable(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.excludes(tile, request)
  }

  def walkable(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
    With.grids.walkable.get(tile) &&
      ! unwalkable.excludes(tile, request) &&
      ! tile.zone.bases.exists(_.townHallArea.contains(tile))
  }

  def gassable(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
    ! ungassable.excludes(tile, request)
  }

  def townhallable(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
    ! untownhallable.excludes(tile, request)
  }
  
  def diffPlacement(tile: Tile, request: PlacementRequest): ArchitectureDiff = {

    val output = new ArchitectureDiffSeries

    val area = TileRectangle(
      tile.add(request.blueprint.relativeBuildStart),
      tile.add(request.blueprint.relativeBuildEnd))
    val exclusion = Exclusion(request.blueprint.toString, area, Some(request))

    area.tiles.filter(_.valid).foreach(new ArchitectureDiffExclude(_, exclusion))

    // If we have no Pylons, place in advance of our first completing
    if (request.blueprint.powers.get && ! With.units.existsOurs(Protoss.Pylon)) {
      output.stack += new ArchitectureDiffPower(tile)
    }

    output
  }
  
  /////////////
  // Margins //
  /////////////

  private def recalculateExclusions() {

    // Reserve addon space in bases
    if (With.self.isTerran) {
      With.geography.bases.foreach(base => {
        val start = base.townHallTile.add(4, 1)
        val addonArea = TileRectangle(start, start.add(2, 2))
        val exclusion = Some(Exclusion("Addon for " + base, addonArea))
        addonArea.tiles.foreach(unbuildable.set(_, exclusion))
      })
    }

    With.units.ours.foreach(unit => {
      // Reserve unit's addon space
      if (unit.unitClass.canBuildAddon) {
        val exclusion = Some(Exclusion("Addon for " + unit, unit.addonArea))
        unit.addonArea.tiles.foreach(unbuildable.set(_, exclusion))
      }

      // Reserve margins for buildings which produce ground units
      if ( ! unit.flying && (unit.isAny(Zerg.Egg, Zerg.LurkerEgg) || (unit.unitClass.isBuilding && usuallyNeedsMargin(unit.unitClass)))) {
        val exclusion = Some(Exclusion("Margin for " + unit, unit.tileArea.expand(1, 1)))
        val w = 1 + unit.unitClass.tileWidth
        val h = 1 + unit.unitClass.tileHeight
        var x = 0
        while(x < w+1) {
          unwalkable.set(unit.tileTopLeft.add(x-1, -1), exclusion)
          unwalkable.set(unit.tileTopLeft.add(x-1, h-1), exclusion)
          x += 1
        }
        var y = 0
        while(y < h+1) {
          unwalkable.set(unit.tileTopLeft.add(-1,  y-1), exclusion)
          unwalkable.set(unit.tileTopLeft.add(w-1, y-1), exclusion)
          y += 1
        }
      }
    })

    // Flag places where we can't build gas
    With.geography.bases.foreach(_.gas.foreach(gas =>
      if (gas.alive && ! gas.player.isNeutral) {
        val exclusion = Some(Exclusion("Geyser occupied by" + gas, gas.tileArea.expand(1, 1)))
        ungassable.set(gas.tileTopLeft, exclusion)
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
