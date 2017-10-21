package Information

import Information.Geography.Types.Base
import Information.Intelligenze.Fingerprinting.Fingerprints
import Information.Intelligenze.UnitsShown
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import Utilities.ByOption

class Intelligence {
  
  val unitsShown    : UnitsShown    = new UnitsShown
  val fingerprints  : Fingerprints  = new Fingerprints
  
  var firstEnemyMain: Option[Base] = None
  
  def leastScoutedBases: Iterable[Base] = leastScoutedBasesCache()
  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()
  
  
  private val mostBaselikeEnemyTileCache = new Cache(() =>
    With.units.enemy
      .toVector
      .filter(unit => unit.possiblyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncludingCenter)
      .headOption
      .getOrElse(leastScoutedBases.head.townHallArea.midpoint))
  
  private val leastScoutedBasesCache = new Cache(() => {
    lazy val enemyBaseHearts = With.geography.enemyBases.map(_.heart)
    lazy val weHaveFliers = With.units.ours.exists(_.flying)
    With.geography.bases
      .toVector
      .filter(base => weHaveFliers || ! base.zone.island)
      .sortBy(_.heart.tileDistanceFast(With.geography.home))
      .sortBy( ! _.isStartLocation)
      .sortBy(base => ByOption.min(enemyBaseHearts.map(_.tileDistanceFast(base.heart))).getOrElse(1.0) / (1.0 + With.framesSince(base.lastScoutedFrame)))
  })
  
  def enemyMain: Option[Base] = {
    firstEnemyMain.filter(base => ! base.scouted || base.owner.isEnemy)
  }
  
  def enemyNatural: Option[Base] = {
    enemyMain.flatMap(_.natural)
  }
  
  def update() {
    unitsShown.update()
    updateEnemyMain()
  }
  
  private def updateEnemyMain() {
    if (firstEnemyMain.isEmpty) {
      firstEnemyMain = With.geography.startBases.find(_.owner.isEnemy)
    }
    if (firstEnemyMain.isEmpty) {
      val possibleMains = With.geography.startBases.filterNot(_.owner.isUs).filter(base => base.owner.isEnemy || ! base.scouted)
      if (possibleMains.size == 1) {
        firstEnemyMain = possibleMains.headOption
      }
    }
  }
}
