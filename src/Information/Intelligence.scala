package Information

import Information.Geography.Types.Base
import Information.Intelligenze.UnitsShown
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class Intelligence {
  
  val unitsShown: UnitsShown = new UnitsShown
  var firstEnemyMain: Option[Base] = None

  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()
  def threatOrigin: Tile = enemyThreatOriginCache()
  
  private val scoutTiles = new mutable.ListBuffer[Tile]
  private var lastScoutFrame = 0
  private var flyingScout = false
  def highlightScout(unit: UnitInfo) {
    if (scoutTiles.size < 5) {
      scoutTiles += unit.tileIncludingCenter
    }
    flyingScout = flyingScout || unit.flying
  }

  private val baseIntrigue = new Cache(() => {
    lazy val enemyBaseHearts = With.geography.enemyBases.map(_.heart)
    if (scoutTiles.isEmpty) {
      scoutTiles.append(With.geography.home)
    }
    With.geography.bases
      .filter( ! _.zone.island || flyingScout || With.geography.ourBases.forall(_.zone.island))
      .map(base => (base, {
        val heartMain = base.heart.pixelCenter
        val heartNatural = base.natural.getOrElse(base).heart.pixelCenter
        val hearts = Vector(heartMain, heartNatural)
        val distanceFromEnemyBase = 32.0 * 32.0 + ByOption.min(enemyBaseHearts.map(_.groundPixels(heartMain))).getOrElse(With.mapPixelWidth.toDouble)
        val informationAge = 1.0 + With.framesSince(base.lastScoutedFrame)
        val startPositionBonus = if (base.isStartLocation && base.lastScoutedFrame <= 0) 100.0 else 1.0
        val output = startPositionBonus * informationAge / distanceFromEnemyBase
        output
      }))
      .toMap
  })

  val mostIntriguingBases: Cache[Vector[Base]] = new Cache(() => baseIntrigue().toVector.sortBy(-_._2).map(_._1))

  // A shared queue of bases to scout
  private val nextBasesToScoutCache = new Cache(() => new mutable.ListBuffer[Base] ++ baseIntrigue().toVector.sortBy(-_._2).map(_._1))
  private def populateNextBasesToScout() {
     if (nextBasesToScoutCache().isEmpty) {
       nextBasesToScoutCache.invalidate()
     }
  }
  def peekNextBaseToScout(filter: Base => Boolean = b => true): Base = {
    populateNextBasesToScout()
    nextBasesToScoutCache().find(filter).getOrElse(nextBasesToScoutCache().head)
  }
  def claimBaseToScout(filter: Base => Boolean = b => true): Base = {
    populateNextBasesToScout()
    val output = peekNextBaseToScout(filter)
    nextBasesToScoutCache() -= output
    output
  }
  def claimBaseToScoutForUnit(unit: FriendlyUnitInfo, filter: Base => Boolean = b => true): Base = {
    populateNextBasesToScout()
    val base = nextBasesToScoutCache().maxBy(base => {
      var distance = 32.0 * 10 + unit.pixelDistanceCenter(base.heart.pixelCenter)
      if ( ! unit.flying && base.zone.island && unit.zone != base.zone) {
        distance *= 10000.0
      }
      baseIntrigue()(base) / distance
    })
    nextBasesToScoutCache() -= base
    base
  }

  private val mostBaselikeEnemyTileCache = new Cache(() =>
    With.units.enemy
      .view
      .filter(unit => unit.possiblyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .toVector
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncludingCenter)
      .headOption
      .getOrElse(baseIntrigue().maxBy(_._2)._1.townHallArea.midpoint))

  private val enemyThreatOriginBaseFactor = 3
  private val enemyThreatOriginCache = new Cache(() => {
    var x = 0
    var y = 0
    var n = 0
    With.units.enemy.foreach(u => if (u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker) {
      x += u.x / 32
      y += u.y / 32
      n += 1
    })

    x += enemyThreatOriginBaseFactor * mostBaselikeEnemyTile.x
    y += enemyThreatOriginBaseFactor * mostBaselikeEnemyTile.y
    n += enemyThreatOriginBaseFactor

    val airCentroid = Tile(x/n, y/n)
    ByOption.minBy(With.units.enemy.view.map(_.tileIncludingCenter))(_.tileDistanceSquared(airCentroid)).getOrElse(airCentroid)
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
    flyingScout = false
    scoutTiles.clear()
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
