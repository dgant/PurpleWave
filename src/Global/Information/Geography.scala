package Global.Information

import Utilities.Cache
import Geometry.TileRectangle
import Startup.With
import bwapi.{Position, TilePosition, UnitType}

import scala.collection.JavaConverters._
import scala.collection.mutable

class Geography {
  
  val isWalkable = new mutable.HashMap[TilePosition, Boolean] {
    override def default(tile: TilePosition): Boolean = {
      (0 to 4).forall(dx =>
        (0 to 4).forall(dy =>
          With.game.isWalkable(tile.getX * 8 + dx, tile.getY * 8 + dy)))}}
  
  def centerPosition:Position = {
    new Position(With.game.mapWidth * 32 / 2, With.game.mapHeight* 32 / 2)
  }
  
  def centerTilePosition:TilePosition = {
    new TilePosition(With.game.mapWidth / 2, With.game.mapHeight / 2)
  }
  
  def isTownHall(unitType:UnitType):Boolean = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  ).contains(unitType)
  
  def ourBaseHalls:Iterable[bwapi.Unit] = {
    With.ourUnits.filter(unit => isTownHall(unit.getType) && ! unit.isFlying)
  }
  
  def ourHarvestingAreas:Iterable[TileRectangle] = { _ourMiningAreasCache.get }
  val _ourMiningAreasCache = new Cache[Iterable[TileRectangle]] {
    duration = 24 * 15
    setCalculator(() => _recalculateOurMiningAreas)
  }
  def _recalculateOurMiningAreas:Iterable[TileRectangle] = {
    ourBaseHalls.map(base => {
      val nearbyUnits = base.getUnitsInRadius(32 * 10).asScala
  
      val minerals = nearbyUnits
        .filter(_.getType == UnitType.Resource_Mineral_Field)
        .map(_.getPosition)
      
      val geysers = nearbyUnits
        .filter(unit => unit.getType.isRefinery || unit.getType == UnitType.Resource_Vespene_Geyser)
        .flatten(unit => List(new Position(unit.getX - 32, unit.getY - 16), new Position(unit.getX + 32, unit.getY + 16)))
      
      val boxedUnits = minerals ++ geysers :+ base.getPosition
      
      //Draw a box around the area
      val top    = boxedUnits.map(_.getY).min + 16
      val bottom = boxedUnits.map(_.getY).max + 16
      val left   = boxedUnits.map(_.getX).min + 16
      val right  = boxedUnits.map(_.getX).max + 16
      
      new TileRectangle(
        new TilePosition(left/32, top/32),
        new TilePosition(right/32, bottom/32))
    })
  }
}
