package Processes

import Startup.With
import Types.EnemyUnitInfo
import bwapi.TilePosition
import bwta.{BWTA, BaseLocation}

import scala.collection.JavaConverters._

class Scout {
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyBuilding:Option[EnemyUnitInfo] = {
    With.tracker.knownEnemyUnits
      .toList
      .filter(unit => unit.getType.isBuilding)
      .sortBy(unit => unit.getType.isFlyer)
      .sortBy(unit => ! With.map.isTownHall(unit.getType))
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => ! base.isStartLocation)
      .sortBy(base => With.game.isExplored(base.getTilePosition))
  }
}
