package Information

import Information.Geography.Types.Base
import Performance.Caching.CacheFrame
import Startup.With
import bwapi.TilePosition

class Intelligence {
  
  def mostBaselikeEnemyPosition:TilePosition = mostBaselikeEnemyPositionCache.get
  val mostBaselikeEnemyPositionCache = new CacheFrame(() =>
    With.units.enemy
      .toList
      .filterNot(_.flying)
      .sortBy(unit => ! unit.unitClass.isBuilding)
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileCenter)
      .headOption
      .getOrElse(leastScoutedBases.head.townHallRectangle.midpoint))
  
  def leastScoutedBases:Iterable[Base] = leastScoutedBasesCache.get
  private val leastScoutedBasesCache = new CacheFrame(() =>
    With.geography.bases
      .toList
      .sortBy( ! _.isStartLocation)
      .sortBy(_.lastScouted))
}
