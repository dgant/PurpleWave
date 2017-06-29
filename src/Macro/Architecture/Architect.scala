package Macro.Architecture

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Architecture.Heuristics.EvaluatePlacements
import Mathematics.Points.{Tile, TileRectangle}
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}

import scala.collection.mutable
import scala.util.Random

class Architect {
  
  private var bases: Array[Base] = Array.empty
  val exclusions: mutable.ArrayBuffer[Exclusion] = new mutable.ArrayBuffer[Exclusion]
  
  def usuallyNeedsMargin(unitClass: UnitClass): Boolean = {
    unitClass.isBuilding &&
    UnitClasses.all.exists(unit => ! unit.isFlyer && unit.whatBuilds._1 == unitClass) &&
    ! unitClass.isTownHall //Nexus margins bork FFEs. Down the road Hatcheries may need margins.
  }
  
  def reboot() {
    bases = With.geography.ourBases.toArray.sortBy(base => - base.mineralsLeft * base.zone.area)
    exclusions.clear()
    exclusions ++= With.geography.bases
      .filterNot(_.owner.isEnemy)
      .map(base => Exclusion(
        "Harvesting area",
        base.harvestingArea,
        gasAllowed = true,
        townHallAllowed = true))
    exclusions ++= With.units.ours
      .filter(unit => usuallyNeedsMargin(unit.unitClass))
      .map(unit => Exclusion(
        "Margin for " + unit,
        unit.tileArea.expand(1, 1),
        gasAllowed = true,
        townHallAllowed = true))
  }
  
  def fulfill(buildingDescriptor: BuildingDescriptor, placement: Option[Placement]): Placement = {
    
    if (placement.isDefined
      && placement.get.tile.isDefined
      && With.frame - placement.get.createdFrame < With.configuration.maxPlacementAge) {
      val tile = placement.get.tile.get
      if (canBuild(buildingDescriptor, tile)) {
        exclude(buildingDescriptor, tile)
        return placement.get
      }
    }
  
    val output = placeBuilding(buildingDescriptor)
    output.tile.foreach(exclude(buildingDescriptor, _))
    output
  }
  
  def canBuild(buildingDescriptor: BuildingDescriptor, tile: Tile): Boolean = {
    if ( ! buildingDescriptor.accepts(tile)) {
      return false
    }
    
    val buildArea = TileRectangle(
      tile.add(buildingDescriptor.relativeBuildStart),
      tile.add(buildingDescriptor.relativeBuildEnd))
    
    if (violatesExclusion(buildingDescriptor, buildArea)) {
      return false
    }
    if ( ! buildingDescriptor.gas && tripsOnUnits(buildingDescriptor, buildArea)) {
      return false
    }
    
    true
  }
  
  private def violatesExclusion(buildingDescriptor: BuildingDescriptor, buildArea: TileRectangle): Boolean = {
    exclusions
      .exists(exclusion =>
        ! (exclusion.gasAllowed       && buildingDescriptor.gas)       &&
        ! (exclusion.townHallAllowed  && buildingDescriptor.townHall)  &&
        exclusion.areaExcluded.intersects(buildArea))
  }
  
  private def tripsOnUnits(buildingDescriptor: BuildingDescriptor, buildArea: TileRectangle): Boolean = {
    var totalWorkers = 0
    buildArea.tiles.foreach(tile =>
      With.grids.units.get(tile).foreach(unit =>
        if (unit.targetPixel.exists(targetPixel => ! buildArea.contains(targetPixel.tileIncluding))) {
          //This is fine. Right?
          //We want to be a little lenient about units being in the way because otherwise we often refuse to take our natural
        }
        else if (unit.isOurs && unit.unitClass.isWorker) {
          totalWorkers += 1
          if (totalWorkers > 1) {
            return true
          }
        }
        else if ( ! buildingDescriptor.gas || ! unit.is(Neutral.Geyser)) {
          return true
        }
        else if ( ! unit.flying) {
          return true
        }
      ))
    false
  }
  
  private def exclude(buildingDescriptor: BuildingDescriptor, tile: Tile) {
    val margin = if (buildingDescriptor.margin) 1 else 0
    exclusions += Exclusion(
      buildingDescriptor.toString,
      TileRectangle(
        tile.add(buildingDescriptor.relativeMarginStart),
        tile.add(buildingDescriptor.relativeMarginEnd)),
      gasAllowed      = false,
      townHallAllowed = false)
  }
  
  private def placeBuilding(
    buildingDescriptor  : BuildingDescriptor,
    exclusions          : Iterable[TileRectangle] = Vector.empty)
      : Placement = {
    
    val allCandidates = candidates(buildingDescriptor)
      .filter(canBuild(buildingDescriptor, _))
      .take(With.configuration.maxGroundskeeperSearches)
      .toVector
    
    val bestCandidate = EvaluatePlacements.best(buildingDescriptor, allCandidates)
    
    bestCandidate
  }
  
  private def candidates(buildingDescriptor: BuildingDescriptor): Iterable[Tile] = {
    
    if (buildingDescriptor.townHall) {
      With.geography.bases
        .filterNot(base => base.owner.isEnemy || base.zone.island)
        .map(_.townHallArea.startInclusive)
    }
    else if (buildingDescriptor.gas) {
      With.geography.bases
        .filter(_.townHall.exists(_.player.isUs))
        .flatMap(_.gas.map(_.tileTopLeft))
    }
    else {
      shuffle(
        With.geography.ourBases
          .flatMap(_.zone.tiles)
          .toList) ++
      shuffle(
        With.geography.zones
          .filter(zone =>
            ! zone.island
            && zone.owner.isNeutral
            && With.geography.ourBases.exists(ourBase => ourBase.heart.groundPixels(zone.centroid) < 32.0 * 50.0))
          .toList
          .flatMap(_.tiles))
    }
  }
  
  private def shuffle(tiles: Iterable[Tile]): Iterable[Tile] = {
    Random.shuffle(tiles)
  }
}
