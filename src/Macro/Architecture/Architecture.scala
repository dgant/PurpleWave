package Macro.Architecture

import Debugging.Visualizations.Views.Geography.ShowArchitecturePlacements
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Types.{Edge, Zone}
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

class Architecture {
  val exclusions        : mutable.ArrayBuffer[Exclusion]            = new mutable.ArrayBuffer[Exclusion]
  val unbuildable       : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val unwalkable        : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val ungassable        : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val untownhallable    : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val creep             : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val powered2Height    : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val powered3Height    : mutable.Set[Tile]                         = new mutable.HashSet[Tile]
  val existingPaths     : mutable.HashMap[Edge, TilePathCache]      = new mutable.HashMap[Edge, TilePathCache]
  var accessibleZones   : Vector[Zone]                              = Vector.empty
  
  class TilePathCache {
    var path  : Option[TilePath]  = None
    var tiles : Set[Tile]         = Set.empty
    var valid : Boolean           = false
    
    def set(newPath: TilePath) {
      path  = Some(newPath)
      tiles = newPath.tiles.map(_.toSet).getOrElse(Set.empty)
      valid = true
    }
  
    def update() {
      if (valid && ! path.exists(_.tiles.exists(_.forall(With.architecture.walkable)))) {
        valid = false
      }
    }
  }
    
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
    unbuildable     .clear()
    unwalkable      .clear()
    ungassable      .clear()
    untownhallable  .clear()
    powered2Height  .clear()
    powered3Height  .clear()
    recalculateExclusions()
    recalculatePower()
    recalculateBuilderAccess()
    updatePaths()
  }
  
  private def updatePaths() {
    existingPaths.values.foreach(_.update())
  }
  
  def buildable(tile: Tile): Boolean = {
    With.grids.buildable.get(tile) && ! unbuildable.contains(tile)
  }
  
  def walkable(tile: Tile): Boolean = {
    With.grids.walkable.get(tile) &&
      ! unwalkable.contains(tile) &&
      ! tile.zone.bases.exists(_.townHallArea.contains(tile))
  }
  
  def assumePlacement(placement: Placement) {
    if (placement.tile.isEmpty) return
    
    val tile = placement.tile.get
  
    val area = TileRectangle(
      tile.add(placement.blueprint.relativeBuildStart),
      tile.add(placement.blueprint.relativeBuildEnd))
    
    unbuildable     ++= area.tiles
    unwalkable      ++= area.tiles
    untownhallable  ++= area.tiles
    ungassable      ++= area.tiles
  
    if (placement.blueprint.powers.get && ! With.units.existsOurs(Protoss.Pylon)) {
      addPower(tile)
    }
  
    updatePaths() //Invalidate any paths which no longer work
    
    if (ShowArchitecturePlacements.inUse) {
      exclusions += Exclusion(placement.blueprint.toString, area)
    }
  }
  
  /////////////
  // Margins //
  /////////////
  
  private def recalculateUnwalkable() {
    unwalkable ++= With.units.all
      .filter(unit => unit.unitClass.isBuilding && ! unit.flying)
      .flatMap(_.tileArea.tiles)
  }
  
  private def recalculateUnbuildable() {
    unbuildable ++= With.units.ours
      .filter(unit =>
        ! unit.flying && (
          unit.isEnemy
          || unit.is(Zerg.Larva)
          || unit.is(Zerg.Egg)
          || unit.is(Zerg.Lurker)
          || unit.unitClass.isBuilding))
      .flatMap(unit =>
        if (usuallyNeedsMargin(unit.unitClass))
          unit.tileArea.expand(1, 1).tiles
        else
          unit.tileArea.tiles)
  }

  private def recalculateExclusions() {
    def forUnbuildable  = With.units.all.view.filter(isGroundBuilding)
    val forUnwalkable   = With.units.ours.toSeq.filter(unit => isGroundBuilding(unit) && usuallyNeedsMargin(unit.unitClass))
    val expansionAddons = if (With.self.isTerran) With.geography.bases.map(base => {
      val start = base.townHallTile.add(4, 1)
      TileRectangle(start, start.add(2, 2))
    }) else Seq.empty
    
    forUnbuildable.foreach(unbuildable ++= _.tileArea.tiles)
    forUnbuildable.filter(_.unitClass.canBuildAddon).foreach(unbuildable ++= _.addonArea.tiles)
    expansionAddons.foreach(unbuildable ++= _.tiles)
    unwalkable      ++= unbuildable
    unwalkable      ++= forUnwalkable.flatMap(_.tileArea.expand(1, 1).tiles)
    untownhallable  ++= unbuildable
    ungassable      ++= With.units.all.filter(unit => ! unit.player.isNeutral && unit.alive && unit.unitClass.isGas).map(_.tileTopLeft)
      
    if (ShowArchitecturePlacements.inUse) {
      exclusions ++= forUnwalkable.map(unit => Exclusion("Margin for " + unit, unit.tileArea.expand(1, 1)))
    }
  }
  
  private def isGroundBuilding(unit: UnitInfo): Boolean = {
    ( ! unit.flying && unit.unitClass.isBuilding) || unit.is(Zerg.Egg) || unit.is(Zerg.LurkerEgg) //Commonly used as map blocks
  }
  
  ///////////
  // Power //
  ///////////
  
  private def recalculatePower() {
    With.units.ours.foreach(unit =>
      if (
        unit.is(Protoss.Pylon)
        && (
          With.framesSince(unit.completionFrame) < GameTime(0, 5)()
          || (
            ! unit.complete
            && unit.zone.units.forall(other => ! other.is(Protoss.Pylon) || ! other.complete)
          )
        )) {
        addPower(unit.tileTopLeft)
      })
  }
  
  private def addPower(tile: Tile) {
    With.grids.psi2Height.psiPoints.map(tile.add).map(neighbor => if (neighbor.valid) powered2Height += neighbor)
    With.grids.psi3Height.psiPoints.map(tile.add).map(neighbor => if (neighbor.valid) powered3Height += neighbor)
  }
  
  /////////////////
  // Walkability //
  /////////////////
  
  private def canaryTile(zone: Zone): Tile = {
    Spiral.points(20)
      .map(zone.centroid.add)
      .filter(_.valid)
      .find(walkable)
      .getOrElse(zone.centroid)
  }

  private def isOurBuilder(u: UnitInfo) = u.isOurs && u.unitClass.isWorker

  private def recalculateBuilderAccess() {
    val hasBuilder = With.geography.zones.filter(_.units.exists(isOurBuilder))
    val accessible = With.geography.zones.filter(z => hasBuilder.exists(_.distanceGrid.get(z.centroid) < Int.MaxValue))
    accessibleZones = (hasBuilder ++ accessible).distinct
  }
}
