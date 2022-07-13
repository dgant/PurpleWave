package Placement.Architecture

import Information.Geography.Types.Zone
import Information.Grids.Versioned.GridVersionedInt
import Lifecycle.With
import ArchitecturalAssessment.ArchitecturalAssessment
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Pylons
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Plasma
import Utilities.LightYear
import Utilities.Time.Forever

class Architecture {
  val unbuildable       : GridExclusion     = new GridExclusion
  val unwalkable        : GridExclusion     = new GridExclusion
  val ungassable        : GridExclusion     = new GridExclusion
  val untownhallable    : GridExclusion     = new GridExclusion
  val creep             : GridExclusion     = new GridExclusion
  val powerFrame2Height : GridVersionedInt  = new GridVersionedInt { override val defaultValue: Int = Forever() }
  val powerFrame3Height : GridVersionedInt  = new GridVersionedInt { override val defaultValue: Int = Forever() }
  var accessibleZones   : Vector[Zone]      = Vector.empty

  def update(): Unit = {
    unbuildable       .update()
    unwalkable        .update()
    ungassable        .update()
    untownhallable    .update()
    powerFrame2Height .update()
    powerFrame3Height .update()
    recalculateExclusions()
    recalculatePower()
    recalculateBuilderAccess()
  }

  def buildable(tile: Tile, request: Option[BuildingPlacement] = None): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.excludes(tile, request)
  }

  def walkable(tile: Tile, request: Option[BuildingPlacement] = None): Boolean = {
    With.grids.walkable.get(tile) &&
      ! unwalkable.excludes(tile, request) &&
      ! tile.zone.bases.exists(_.townHallArea.contains(tile))
  }

  def gassable(tile: Tile, request: Option[BuildingPlacement] = None): Boolean = {
    ! ungassable.excludes(tile, request)
  }

  def townhallable(tile: Tile, request: Option[BuildingPlacement] = None): Boolean = {
    ! untownhallable.excludes(tile, request)
  }

  def assumePlacement(tile: Tile, unit: UnitClass, futureFrames: Int = 0): Unit = {
    val area = TileRectangle(tile, tile.add(unit.tileWidth, unit.tileHeight))
    val exclusion = Exclusion(unit.toString, area, Some(BuildingPlacement(tile, unit)))

    area.tiles.filter(_.valid).foreach(t => {
      With.architecture.unbuildable.set(tile, Some(exclusion))
      With.architecture.unwalkable.set(tile, Some(exclusion))
      With.architecture.untownhallable.set(tile, Some(exclusion))
      With.architecture.ungassable.set(tile, Some(exclusion))
    })

    if (unit == Protoss.Pylon) {
      val powerFrame = With.frame + futureFrames + Protoss.Pylon.buildFramesFull
      Pylons.points2.map(tile.add).filter(_.valid).foreach(With.architecture.powerFrame2Height.set(_, powerFrame))
      Pylons.points3.map(tile.add).filter(_.valid).foreach(With.architecture.powerFrame3Height.set(_, powerFrame))
    }
  }

  /////////////
  // Margins //
  /////////////

  private def recalculateExclusions(): Unit = {

    // Reserve addon space in bases
    if (With.self.isTerran) {
      With.geography.bases.foreach(base => {
        val start = base.townHallTile.add(4, 1)
        val addonArea = TileRectangle(start, start.add(2, 2))
          val exclusion = Some(Exclusion("Addon (CC)", addonArea))
          addonArea.tiles.foreach(unbuildable.set(_, exclusion))
      })
    }

    With.units.ours.foreach(unit => {
      // Reserve unit's addon space
      if (unit.unitClass.canBuildAddon) {
        val exclusion = Some(Exclusion("Addon", unit.addonArea))
        unit.addonArea.tiles.foreach(unbuildable.set(_, exclusion))
      }

      // Don't get stuck trying to build on top of eggs
      if (unit.isAny(Zerg.Egg, Zerg.LurkerEgg)) {
        val exclusion = Some(Exclusion(unit.unitClass.toString, unit.tileArea.expand(1, 1)))
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
        val exclusion = Some(Exclusion("Geyser occupied", gas.tileArea))
        ungassable.set(gas.tileTopLeft, exclusion)
      }
    ))
  }

  ///////////
  // Power //
  ///////////

  private def recalculatePower(): Unit = {
    With.units.ours.filter(Protoss.Pylon).foreach(unit => addPower(unit.tileTopLeft, unit.completionFrameFull))
  }

  private def addPower(tile: Tile, frame: Int): Unit = {
    Pylons.points2.map(tile.add).foreach(neighbor => if (neighbor.valid) powerFrame2Height.set(neighbor, frame))
    Pylons.points3.map(tile.add).foreach(neighbor => if (neighbor.valid) powerFrame3Height.set(neighbor, frame))
  }

  private def recalculateBuilderAccess(): Unit = {
    val hasBuilder = With.geography.zones.filter(_.units.exists(u => u.isOurs && u.unitClass.isWorker))
    val accessible = With.geography.zones.filter(z => hasBuilder.exists(_.heart.groundPixels(z.heart) < LightYear()))
    accessibleZones = (hasBuilder ++ accessible).distinct
  }

  ///////////////
  // Legality! //
  ///////////////

  def assess(topLeft: Tile, building: UnitClass, futureFrames: Int = 0): ArchitecturalAssessment = {
    val width: Int = building.tileWidth + (if (building.canBuildAddon && ! building.isTownHall) 2 else 0)
    val height: Int = building.tileHeight

    if ( ! topLeft.valid) {
      return ArchitecturalAssessment.Invalid
    }
    if (building.requiresPsi) {
      // We can allow use of forthcoming Pylon power by using a value higher than With.frame
      if (height == 3 && ! With.grids.psi3Height.isSet(topLeft) && powerFrame3Height.get(topLeft) > With.frame + futureFrames) {
        return ArchitecturalAssessment.Unpowered
      }
      if (height == 2 && ! With.grids.psi2Height.isSet(topLeft) && powerFrame2Height.get(topLeft) > With.frame + futureFrames) {
        return ArchitecturalAssessment.Unpowered
      }
    }

    val zone = topLeft.zone
    if (zone.island
      && ! Plasma()
      && ( ! With.blackboard.allowIslandBases() || ! zone.bases.exists(_.minerals.exists(_.visible)))
      && ! accessibleZones.contains(zone)) {
      return ArchitecturalAssessment.InaccessibleIsland
    }

    val request = BuildingPlacement(topLeft, building)

    if (building.isGas) {
      if ( ! zone.bases.exists(_.gas.exists(_.tileTopLeft == topLeft))) {
        return ArchitecturalAssessment.IsntGas
      }
      if ( ! gassable(topLeft, Some(request))) {
        return ArchitecturalAssessment.BlockedGas
      }
      return ArchitecturalAssessment.Accepted
    }

    if (building.isTownHall) {
      if ( ! zone.bases.exists(_.townHallTile == topLeft)) {
        return ArchitecturalAssessment.IsntBasePosition
      }
      if ( ! townhallable(topLeft, Some(request))) {
        return ArchitecturalAssessment.IsntLegalForTownHall
      }
    }

    val buildArea = TileRectangle(topLeft, topLeft.add(width, height))
    var x = buildArea.startInclusive.x
    val xMax = buildArea.endExclusive.x
    val yMax = buildArea.endExclusive.y
    if ( ! With.grids.buildableW(width).get(buildArea.startInclusive)) {
      return ArchitecturalAssessment.IsntBuildable
    }
    while (x < xMax) {
      var y = buildArea.startInclusive.y
      while (y < yMax) {
        val nextTile = Tile(x, y)
        if ( ! buildable(nextTile, Some(request))) {
          return ArchitecturalAssessment.IsntBuildable
        }
        if (building.requiresCreep != nextTile.creep) {
          return ArchitecturalAssessment.CreepMismatch
        }
        if (building.isTownHall && ! With.grids.buildableTownHall.get(nextTile)) {
          return ArchitecturalAssessment.ViolatesResourceGap
        }
        if ( ! building.isTownHall && With.grids.units.get(nextTile).exists(u => ! u.flying && u.isEnemy || ! u.canMove)) {
          return ArchitecturalAssessment.BlockedByUnit
        }
        y += 1
      }
      x += 1
    }
    ArchitecturalAssessment.Accepted
  }
}
