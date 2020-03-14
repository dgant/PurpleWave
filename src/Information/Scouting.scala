package Information

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import Planning.UnitMatchers.{UnitMatchBuilding, UnitMatchWorkers}
import Utilities.{ByOption, CountMap}

class Scouting {

  private val baseScoutMap = new CountMap[Base]
  def baseScouts(base: Base): Int = baseScoutMap(base)
  def registerScout(base: Base): Unit = {
    baseScoutMap(base) += 1
  }

  def baseIntrigue: Map[Base, Double] = cacheBaseIntrigueInitial().map(p => (p._1, p._2 / Math.pow(100.0, baseScouts(p._1))))
  private val cacheBaseIntrigueInitial = new Cache(() => With.geography.bases.filter(b => ! b.owner.isUs).map(base => (base, getBaseIntrigueInitial(base))).toMap)
  private def getBaseIntrigueInitial(base: Base): Double = {
    val enemyHearts         = With.geography.enemyBases.map(_.heart)
    val heartMain           = base.heart.pixelCenter
    val heartNatural        = base.natural.getOrElse(base).heart.pixelCenter
    val hearts              = Vector(heartMain, heartNatural)
    val distanceFromEnemy   = 32.0 * 32.0 + ByOption.min(enemyHearts.map(_.groundPixels(heartMain))).getOrElse(With.mapPixelWidth.toDouble)
    val informationAge      = 1.0 + With.framesSince(base.lastScoutedFrame)
    val startPositionBonus  = if (base.isStartLocation && base.lastScoutedFrame <= 0) 100.0 else 1.0
    val output              = startPositionBonus * informationAge / distanceFromEnemy
    output
  }

  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()
  private val mostBaselikeEnemyTileCache = new Cache(() =>
    With.units.enemy
      .view
      .filter(unit => unit.possiblyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .toVector
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncludingCenter)
      .headOption
      .getOrElse(cacheBaseIntrigueInitial().maxBy(_._2)._1.townHallArea.midpoint))

  def threatOrigin: Tile = threatOriginCache()
  private val threatOriginCache = new Cache(() => {
    var x = 0
    var y = 0
    var n = 0
    With.units.enemy.foreach(u => if (u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker) {
      x += u.x / 32
      y += u.y / 32
      n += 1
    })
    val enemyThreatOriginBaseFactor = 3
    x += enemyThreatOriginBaseFactor * mostBaselikeEnemyTile.x
    y += enemyThreatOriginBaseFactor * mostBaselikeEnemyTile.y
    n += enemyThreatOriginBaseFactor
    val airCentroid = Tile(x/n, y/n)
    ByOption.minBy(With.units.enemy.view.map(_.tileIncludingCenter))(_.tileDistanceSquared(airCentroid)).getOrElse(airCentroid)
  })

  def enemyMain: Option[Base] = firstEnemyMain.filter(base => ! base.scouted || base.owner.isEnemy)
  def enemyNatural: Option[Base] = enemyMain.flatMap(_.natural)
  private var firstEnemyMain: Option[Base] = None

  def enemyHasScoutedUs: Boolean = _enemyHasScoutedUs
  def enemyHasScoutedUsWithWorker: Boolean = _enemyHasScoutedUsWithWorker
  private var _enemyHasScoutedUs = false
  private var _enemyHasScoutedUsWithWorker = false
  
  def update() {
    baseScoutMap.clear()
    if (firstEnemyMain.isEmpty) {
      firstEnemyMain = With.geography.startBases.find(_.owner.isEnemy)
    }
    if (firstEnemyMain.isEmpty) {
      val possibleMains = With.geography.startBases.filterNot(_.owner.isUs).filter(base => base.owner.isEnemy || ! base.scouted)
      if (possibleMains.size == 1) {
        firstEnemyMain = possibleMains.headOption
      }
    }
    _enemyHasScoutedUsWithWorker = _enemyHasScoutedUsWithWorker || With.geography.ourBases.exists(_.units.exists(u => u.isEnemy && u.is(UnitMatchWorkers)))
    _enemyHasScoutedUs = _enemyHasScoutedUs || _enemyHasScoutedUsWithWorker || With.units.ours.view.filter(_.is(UnitMatchBuilding)).exists(u => u.tileArea.tiles.exists(With.grids.enemyVision.isSet))
  }
}
