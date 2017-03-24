package Information

import Performance.Caching.Cache
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Startup.With
import bwapi.TilePosition

class Intelligence {
  
  def enemyBases:Option[TilePosition] =
    With.units.enemy
      .toList
      .filter(_.unitClass.isBuilding)
      .filter(! _.flying)
      .filter(_.unitClass.isTownHall)
      .map(_.tileTopLeft)
      .headOption
  
  def mostBaselikeEnemyBuilding:Option[ForeignUnitInfo] = {
    With.units.enemy
      .toList
      .filter(unit => unit.unitClass.isBuilding)
      .sortBy(unit => unit.unitClass.isFlyer)
      .sortBy(unit => unit.unitClass.isTownHall)
      .headOption
  }
  
  def leastScoutedBases():Iterable[TilePosition] = leastScoutedBasesCache.get
  private val leastScoutedBasesCache = new Cache(2, () => leastScoutedBasesCalculate)
  private def leastScoutedBasesCalculate:Iterable[TilePosition] =
    With.geography.bases
      .toList
      .sortBy( ! _.isStartLocation)
      .sortBy(base => With.game.isExplored(base.townHallRectangle.midpoint))
      .map(_.townHallRectangle.midpoint)
}
