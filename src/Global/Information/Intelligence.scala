package Global.Information

import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import Utilities.Caching.Cache
import Utilities.Enrichment.EnrichUnitType._
import bwapi.TilePosition

import scala.collection.JavaConverters._

class Intelligence {
  
  def unexploredStartLocations():Iterable[TilePosition] = {
    With.game.getStartLocations.asScala.filterNot(With.game.isExplored)
  }
  
  def enemyBases:Option[TilePosition] =
    With.units.enemy
      .toList
      .filter(_.utype.isBuilding)
      .filter(! _.flying)
      .filter(_.utype.isTownHall)
      .map(_.tileTopLeft)
      .headOption
  
  def mostBaselikeEnemyBuilding:Option[ForeignUnitInfo] = {
    With.units.enemy
      .toList
      .filter(unit => unit.utype.isBuilding)
      .sortBy(unit => unit.utype.isFlyer)
      .sortBy(unit => unit.utype.isTownHall)
      .headOption
  }
  
  def leastScoutedBases():Iterable[TilePosition] = _leastScoutedBasesCache.get
  val _leastScoutedBasesCache = new Cache(24, () => _leastScoutedBases)
  def _leastScoutedBases:Iterable[TilePosition] =
    With.geography.bases.toList.sortBy( ! _.isStartLocation).sortBy(base => With.game.isExplored(base.centerTile)).map(_.centerTile)
}
