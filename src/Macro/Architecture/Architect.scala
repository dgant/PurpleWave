package Macro.Architecture

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.Shapes.Spiral

import scala.collection.mutable

class Architect {
  
  private var bases: Array[Base] = Array.empty
  val exclusions: mutable.ArrayBuffer[TileRectangle] = new mutable.ArrayBuffer[TileRectangle]
  
  def reboot() {
    bases = With.geography.ourBases.toArray.sortBy(base => - base.mineralsLeft * base.zone.area)
    exclusions.clear()
    exclusions ++= bases.map(_.harvestingArea)
    exclusions ++= With.units.ours.filter(_.unitClass.isBuilding).map(_.tileArea)
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
    buildingDescriptor.accepts(tile) && ! violatesExclusion(buildingDescriptor, tile)
  }
  
  private def violatesExclusion(buildingDescriptor: BuildingDescriptor, tile: Tile): Boolean = {
    val buildArea = TileRectangle(
      tile.add(buildingDescriptor.buildStart),
      tile.add(buildingDescriptor.buildEnd))
    
    exclusions.exists(_.intersects(buildArea))
  }
  
  private def exclude(buildingDescriptor: BuildingDescriptor, tile: Tile) {
    val margin = if (buildingDescriptor.margin) 1 else 0
    exclusions += TileRectangle(
      tile.add(buildingDescriptor.marginStart),
      tile.add(buildingDescriptor.marginEnd))
  }
  
  private def placeBuilding(
    buildingDescriptor  : BuildingDescriptor,
    exclusions          : Iterable[TileRectangle] = Vector.empty,
    searchRadius        : Int                     = 30)
      : Option[Tile] = {
    
    val points = bases.map(base =>
        Spiral
          .points(searchRadius)
          .view
          .map(base.heart.add))
      .flatten
      
    points.find(tile => buildingDescriptor.accepts(tile))
  }
}
