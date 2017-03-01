package Global.Information

import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import Utilities.Enrichment.EnrichUnitType._
import bwapi.TilePosition

import scala.collection.JavaConverters._

class Intelligence {
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def mostBaselikeEnemyBuilding:Option[ForeignUnitInfo] = {
    With.units.enemy
      .toList
      .filter(unit => unit.utype.isBuilding)
      .sortBy(unit => unit.utype.isFlyer)
      .sortBy(unit => unit.utype.isTownHall)
      .headOption
  }
  
  def mostUnscoutedBases():Iterable[TilePosition] = {
    (With.game.getStartLocations.asScala ++ With.game.getStaticGeysers.asScala.map(_.getTilePosition))
      .sortBy(base => With.game.isExplored(base))
  }
}
