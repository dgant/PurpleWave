package Global.Information

import Startup.With
import Types.EnemyUnitInfo
import bwapi.TilePosition
import bwta.{BWTA, BaseLocation}

import scala.collection.JavaConverters._

class Intelligence {
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyBuilding:Option[EnemyUnitInfo] = {
    With.memory.knownEnemyUnits
      .toList
      .filter(unit => unit.getType.isBuilding)
      .sortBy(unit => unit.getType.isFlyer)
      .sortBy(unit => ! With.geography.isTownHall(unit.getType))
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[BaseLocation] = {
    BWTA.getBaseLocations.asScala
      .sortBy(base => ! base.isStartLocation)
      .sortBy(base => With.game.isExplored(base.getTilePosition))
  }
}
