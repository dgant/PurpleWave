package Information

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.CacheFrame
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClass.UnitClass
import bwapi.UnitCommandType

import scala.collection.mutable

class Intelligence {
  
  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache.get
  private val mostBaselikeEnemyTileCache = new CacheFrame(() =>
    With.units.enemy
      .toVector
      .filter(unit => unit.possiblyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncludingCenter)
      .headOption
      .getOrElse(leastScoutedBases.head.townHallArea.midpoint))
  
  def leastScoutedBases: Iterable[Base] = leastScoutedBasesCache.get
  private val leastScoutedBasesCache = new CacheFrame(() => {
    lazy val weHaveFliers = With.units.ours.exists(_.flying)
    With.geography.bases
      .toVector
      .filter(base => weHaveFliers || ! base.zone.island)
      .sortBy(_.heart.tileDistanceFast(With.geography.home))
      .sortBy( ! _.isStartLocation)
      .sortBy(_.lastScoutedFrame)
  })
  
  def enemyHasShown(unitClass: UnitClass): Boolean = enemyHasShownUnit(unitClass)
  private val enemyHasShownUnit = new mutable.HashSet[UnitClass]
  
  var firstEnemyMain: Option[Base] = None
  
  def enemyMain: Option[Base] = {
    firstEnemyMain.filter(base => ! base.scouted || base.owner.isEnemy)
  }
  
  def enemyNatural: Option[Base] = {
    enemyMain.flatMap(_.natural)
  }
  
  def update() {
    updateEnemyMain()
    updateZergExpansionAttempt()
  }
  
  private def updateEnemyMain() {
    if (firstEnemyMain.isEmpty) {
      firstEnemyMain = With.geography.startBases.find(_.owner.isEnemy)
    }
    if (firstEnemyMain.isEmpty) {
      val possibleMains = With.geography.startBases.filter(base => base.owner.isEnemy || ! base.scouted)
      if (possibleMains.size == 1) {
        firstEnemyMain = possibleMains.headOption
      }
    }
  }
  
  private def updateZergExpansionAttempt() {
    enemyHasShownUnit ++= With.units.enemy.map(_.unitClass)
    if (With.units.enemy.exists(unit =>
      unit.is(Zerg.Drone) &&
      unit.command.exists(_.getUnitCommandType == UnitCommandType.Build) &&
      unit.targetPixel.exists(_.zone.bases.exists(_.owner.isNeutral)))) {
      With.blackboard.zergWasTryingToExpand = true
    }
  }
}
