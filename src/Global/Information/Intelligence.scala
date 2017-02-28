package Global.Information

import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import bwapi.TilePosition
import bwta.{BWTA, BaseLocation}
import Utilities.Enrichment.EnrichUnitType._

import scala.collection.JavaConverters._

class Intelligence {
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyBuilding:Option[ForeignUnitInfo] = {
    With.units.enemy
      .toList
      .filter(unit => unit.unitType.isBuilding)
      .sortBy(unit => unit.unitType.isFlyer)
      .sortBy(unit => unit.unitType.isTownHall)
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => ! base.isStartLocation)
      .sortBy(base => With.game.isExplored(base.getTilePosition))
  }
}
