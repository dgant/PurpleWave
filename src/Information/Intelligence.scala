package Information

import Information.Geography.Types.Base
import Performance.Caching.CacheFrame
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.UnitClass.UnitClass

import scala.collection.mutable

class Intelligence {
  
  def mostBaselikeEnemyTile:Tile = mostBaselikeEnemyTileCache.get
  val mostBaselikeEnemyTileCache = new CacheFrame(() =>
    With.units.enemy
      .toVector
      .filter(unit => unit.possiblyStillThere && ! unit.flying)
      .sortBy(unit => ! unit.unitClass.isBuilding)
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncludingCenter)
      .headOption
      .getOrElse(leastScoutedBases.head.townHallArea.midpoint))
  
  def leastScoutedBases:Iterable[Base] = leastScoutedBasesCache.get
  private val leastScoutedBasesCache = new CacheFrame(() =>
    With.geography.bases
      .toVector
      .sortBy(_.heart.tileDistanceFast(With.geography.home))
      .sortBy( ! _.isStartLocation)
      .sortBy(_.lastScoutedFrame))
  
  def enemyHasShown(unitClass: UnitClass): Boolean = enemyHasShownUnit(unitClass)
  private val enemyHasShownUnit = new mutable.HashSet[UnitClass]
  def update() {
    enemyHasShownUnit ++= With.units.enemy.map(_.unitClass)
  }
}
