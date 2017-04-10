package ProxyBwapi.UnitTracking

import Mathematics.Shapes.Circle
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Lifecycle.With
import Mathematics.Positions.TileRectangle
import Utilities.EnrichPosition._
import bwapi.{Position, TilePosition, UnitType}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class UnitTracker {
  
  private val friendlyUnitTracker = new FriendlyUnitTracker
  private val foreignUnitTracker = new ForeignUnitTracker
  
  val invalidUnitTypes = Set(
    UnitType.None,
    UnitType.Unknown
  )
  
  def alive(id: Int): Boolean = getId(id).exists(_.alive)
  
  def getId(id: Int): Option[UnitInfo] = friendlyUnitTracker.get(id).orElse(foreignUnitTracker.get(id))
  
  def get(unit: bwapi.Unit): Option[UnitInfo] = if (unit == null) None else getId(unit.getID)
  
  def all: Set[UnitInfo] = ours ++ enemy ++ neutral
  
  def buildings: Set[UnitInfo] = all.filter(_.unitClass.isBuilding)
  
  def ours: Set[FriendlyUnitInfo] = friendlyUnitTracker.ourUnits
  
  def enemy: Set[ForeignUnitInfo] = foreignUnitTracker.enemyUnits
  
  def neutral: Set[ForeignUnitInfo] = foreignUnitTracker.neutralUnits
  
  private def remap(units: java.util.List[bwapi.Unit]): Iterable[UnitInfo] = {
    units.asScala.flatMap(get).toList
  }
  
  def inTileRadius(tile: TilePosition, tiles: Int): Set[UnitInfo] = {
    inTilesNonDistinct(Circle.points(tiles).map(tile.add)).toSet
  }
  
  def inPixelRadius(pixel: Position, pixels: Int): Set[UnitInfo] = {
    val tile = pixel.tileIncluding
    inTilesNonDistinct(Circle.points(pixels / 32 + 1).map(tile.add))
      .filter(_.pixelCenter.pixelDistanceSquared(pixel) <= pixels * pixels)
      .toSet
  }
  
  def inRectangle(rectangle: TileRectangle): Set[UnitInfo] = {
    inTilesNonDistinct(rectangle.tiles).toSet
  }
  
  private def inTilesNonDistinct(tiles:Iterable[TilePosition]):ListBuffer[UnitInfo] = {
    val output = new ListBuffer[UnitInfo]
    tiles.foreach(tile => output ++= With.grids.units.get(tile))
    output
  }
  
  def onFrame() {
    friendlyUnitTracker.onFrame()
    foreignUnitTracker.onFrame()
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    friendlyUnitTracker.onUnitDestroy(unit)
    foreignUnitTracker.onUnitDestroy(unit)
  }
}
