package Macro.Architecture

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral

import scala.collection.mutable

class Architect {
  
  private var bases: Array[Base] = Array.empty
  val exclusions: mutable.ArrayBuffer[Exclusion] = new mutable.ArrayBuffer[Exclusion]
  
  def reboot() {
    bases = With.geography.ourBases.toArray.sortBy(base => - base.mineralsLeft * base.zone.area)
    exclusions.clear()
    exclusions ++= With.geography.bases
      .filterNot(_.owner.isEnemy)
      .map(base => Exclusion("Harvesting area", base.harvestingArea))
  }
  
  def fulfill(buildingDescriptor: BuildingDescriptor, tile: Option[Tile]): Option[Tile] = {
    
    if (tile.isDefined && canBuild(buildingDescriptor, tile.get)) {
      exclude(buildingDescriptor, tile.get)
      return tile
    }
  
    val output = placeBuilding(buildingDescriptor)
    output.foreach(exclude(buildingDescriptor, _))
    output
  }
  
  private def canBuild(buildingDescriptor: BuildingDescriptor, tile: Tile): Boolean = {
    
    if ( ! buildingDescriptor.accepts(tile)) {
      return false
    }
    
    val buildArea = TileRectangle(
      tile.add(buildingDescriptor.buildStart),
      tile.add(buildingDescriptor.buildEnd))
    
    if (violatesExclusion(buildingDescriptor, buildArea)) {
      return false
    }
    if (tripsOnUnits(buildingDescriptor, buildArea)) {
      return false
    }
    true
  }
  
  private def violatesExclusion(buildingDescriptor: BuildingDescriptor, buildArea: TileRectangle): Boolean = {
    exclusions.exists(_.area.intersects(buildArea))
  }
  
  private def tripsOnUnits(buildingDescriptor: BuildingDescriptor, buildArea: TileRectangle): Boolean = {
    var totalWorkers = 0
    buildArea.tiles.foreach(tile =>
      With.grids.units.get(tile).foreach(unit =>
        if (unit.isOurs && unit.unitClass.isWorker) {
          totalWorkers += 1
          if (totalWorkers > 1) {
            return true
          }
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
        tile.add(buildingDescriptor.marginStart),
        tile.add(buildingDescriptor.marginEnd)))
  }
  
  private def placeBuilding(
    buildingDescriptor  : BuildingDescriptor,
    exclusions          : Iterable[TileRectangle] = Vector.empty,
    searchRadius        : Int                     = 30)
      : Option[Tile] = {
    
    val points = bases.flatMap(base =>
      Spiral
        .points(searchRadius)
        .view
        .map(base.heart.add))
      
    points.find(canBuild(buildingDescriptor, _))
  }
}
