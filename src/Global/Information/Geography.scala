package Global.Information

import Geometry.TileRectangle
import Startup.With
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Cache
import bwapi.{Position, TilePosition, UnitType}

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
  
  def home:Position = {
    ourBaseHalls.view.map(_.position).headOption
      .getOrElse(With.units.ours.view.filter(_.unitType.isBuilding).map(_.position).headOption
      .getOrElse(new Position(0,0)))
  }
  
  def ourBaseHalls:Iterable[FriendlyUnitInfo] = {
    With.units.ours.filter(unit => isTownHall(unit.unitType) && ! unit.flying)
  }
  
  def ourHarvestingAreas:Iterable[TileRectangle] = { _ourMiningAreasCache.get }
  val _ourMiningAreasCache = new Cache[Iterable[TileRectangle]] {
    duration = 24 * 15
    setCalculator(() => _recalculateOurMiningAreas)
  }
  def _recalculateOurMiningAreas:Iterable[TileRectangle] = {
    ourBaseHalls.map(base => {
      val nearbyUnits = With.units.inRadius(base.position, 32 * 10)
  
      val minerals = nearbyUnits
        .filter(_.unitType == UnitType.Resource_Mineral_Field)
        .map(_.position)
      
      val geysers = nearbyUnits
        .filter(unit => unit.unitType.isRefinery || unit.unitType == UnitType.Resource_Vespene_Geyser)
        .flatten(unit => List(new Position(unit.left - 16, unit.top), new Position(unit.right + 16, unit.bottom)))
      
      val boxedUnits = minerals ++ geysers ++ Iterable(base.position)
      
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
  
  val _basePositionsCache = new Cache[Iterable[TilePosition]] {
    duration = 24 * 20
    setCalculator(() => _calculateBasePositions)
  }
  def basePositions:Iterable[TilePosition] = _basePositionsCache.get
  def _calculateBasePositions:Iterable[TilePosition] = {
    //With.units.neutral.filter(unit => unit.isMinerals || unit.isGas)
    List.empty
  }
}
