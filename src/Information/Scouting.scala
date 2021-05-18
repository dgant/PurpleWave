package Information

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import Performance.Tasks.TimedTask
import Planning.UnitMatchers.{MatchBuilding, MatchWorker}
import ProxyBwapi.Races.Zerg
import Utilities._

class Scouting extends TimedTask {

  private val baseScoutMap = new CountMap[Base]
  def baseScouts(base: Base): Int = baseScoutMap(base)
  def registerScout(base: Base): Unit = {
    baseScoutMap(base) += 1
  }

  def baseIntrigue: Map[Base, Double] = baseIntrigueRaw.map(p => (p._1, p._2 / Math.pow(100.0, baseScouts(p._1))))
  def baseIntrigueRaw: Map[Base, Double] = cacheBaseIntrigueInitial()
  private val cacheBaseIntrigueInitial = new Cache(() => scoutableBases.map(base => (base, getBaseIntrigueInitial(base))).toMap)
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
  private def scoutableBases: Seq[Base] = {
    var output = With.geography.bases.filter(b => ! b.owner.isUs)
    if (output.isEmpty) {
      output = With.geography.bases
    }
    output
  }

  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()
  private val mostBaselikeEnemyTileCache = new Cache(() =>
    With.units.enemy
      .view
      .filter(unit => unit.likelyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .toVector
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tile)
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
    ByOption.minBy(With.units.enemy.view.map(_.tile))(_.tileDistanceSquared(airCentroid)).getOrElse(airCentroid)
  })

  def firstEnemyMain: Option[Base] = _firstEnemyMain
  def enemyMain: Option[Base] = _firstEnemyMain.filter(base => ! base.scouted || base.owner.isEnemy)
  def enemyNatural: Option[Base] = enemyMain.flatMap(_.natural)
  private var _firstEnemyMain: Option[Base] = None

  def enemyHasScoutedUs: Boolean = _enemyHasScoutedUs
  def enemyHasScoutedUsWithWorker: Boolean = _enemyHasScoutedUsWithWorker
  private var _enemyHasScoutedUs = false
  private var _enemyHasScoutedUsWithWorker = false

  val basesToLookForEnemyScouts = new Cache(() => (With.geography.ourBases :+ With.geography.ourNatural).distinct)
  val enemyScouts = new Cache(() => With.units.enemy.filter(u => u.isAny(Zerg.Overlord, MatchWorker) && u.likelyStillThere && u.base.exists(basesToLookForEnemyScouts().contains)))

  override protected def onRun(budgetMs: Long): Unit = {
    baseScoutMap.clear()
    if (_firstEnemyMain.isEmpty) {
      _firstEnemyMain = With.geography.startBases.find(_.owner.isEnemy)
    }
    if (_firstEnemyMain.isEmpty) {
      val possibleMains = With.geography.startBases.filterNot(_.owner.isUs).filter(base => base.owner.isEnemy || ! base.scouted)
      // Infer possible mains from process of elimination
      if (possibleMains.size == 1) {
        _firstEnemyMain = possibleMains.headOption
        With.logger.debug("Inferred enemy main from process of elimination.")
      }
      // Infer main by creep
      else if (With.frame < Minutes(5)() && With.enemies.exists(_.isZerg)) {
        val newlyCreepedBases = With.geography.startBases.filter(b => b.owner.isNeutral && b.zone.tiles.exists(_.creep))
        if (newlyCreepedBases.size == 1) {
          _firstEnemyMain = newlyCreepedBases.headOption
          With.logger.debug("Inferred enemy main from presence of creep: " + _firstEnemyMain.get.toString)
        }
      }
      // Infer main by Overlord
      else if (With.frame < GameTime(2, 30)()) {
        val overlords = With.units.enemy.filter(Zerg.Overlord)
        val overlordMains = overlords.map(overlord => (overlord, With.geography.startBases.filter(base =>
          base.owner.isNeutral
          && overlord.framesToTravelTo(base.townHallArea.midPixel) < With.frame + Seconds(5)()
        )))
        val overlordProofs = overlordMains.find(_._2.size == 1)
        overlordProofs.foreach(overlordProof => {
          _firstEnemyMain = overlordProof._2.headOption
          With.logger.debug("Inferred enemy main from Overlord position: " + overlordProof._1 + " -> " + _firstEnemyMain.get.toString)
        })
      }
    }
    _enemyHasScoutedUsWithWorker = _enemyHasScoutedUsWithWorker || With.geography.ourBases.exists(_.units.exists(u => u.isEnemy && u.is(MatchWorker)))
    _enemyHasScoutedUs = _enemyHasScoutedUs || _enemyHasScoutedUsWithWorker || With.units.ours.view.filter(MatchBuilding).exists(u => u.tileArea.tiles.exists(With.grids.enemyVision.inRange))
  }
}
