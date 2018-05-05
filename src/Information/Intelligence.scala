package Information

import Information.Geography.Types.Base
import Information.Intelligenze.Fingerprinting.Fingerprints
import Information.Intelligenze.UnitsShown
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

import scala.collection.mutable

class Intelligence {
  
  val unitsShown    : UnitsShown    = new UnitsShown
  val fingerprints  : Fingerprints  = new Fingerprints
  
  var firstEnemyMain: Option[Base] = None
  
  def leastScoutedBases: Iterable[Base] = leastScoutedBasesCache()
  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()
  
  // A shared queue of bases to scout
  private val nextBasesToScout = new Cache(() => new mutable.Queue[Base] ++ leastScoutedBases)
  def nextBaseToScout: Base = {
    val queue = nextBasesToScout()
    if (queue.isEmpty) queue ++= leastScoutedBases
    val output = queue.dequeue()
    output
  }
  
  private val scoutTiles = new mutable.ListBuffer[Tile]
  private var lastScoutFrame = 0
  private var flyingScout = false
  def highlightScout(unit: UnitInfo) {
    if (scoutTiles.size < 5) {
      scoutTiles += unit.tileIncludingCenter
    }
    flyingScout = flyingScout || unit.flying
  }
  
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
    if (scoutTiles.isEmpty) {
      scoutTiles.append(With.geography.home)
    }
    With.geography.bases
      .toVector
      .filter( ! _.zone.island || flyingScout)
      .sortBy(base => {
        val heartMain = base.heart.pixelCenter
        val heartNatural = base.natural.getOrElse(base).heart.pixelCenter
        val hearts = Vector(heartMain, heartNatural)
        val distanceFromEnemyBase = ByOption.min(enemyBaseHearts.map(_.groundPixels(heartMain))).getOrElse(1.0)
        val distanceFromScout =
          if (flyingScout) {
            hearts.map(heart => scoutTiles.map(_.pixelCenter.pixelDistance(heart)).min).sum
          }
          else {
            hearts.map(heart => scoutTiles.map(_.groundPixels(heart)).min).sum
          }
        val informationAge = 1.0 + With.framesSince(base.lastScoutedFrame)
        val output = (distanceFromScout + distanceFromEnemyBase) / informationAge
        output
      })
      .sortBy(base => ! (base.isStartLocation && base.lastScoutedFrame <= 0))
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
    fingerprints.all.foreach(_.update())
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
