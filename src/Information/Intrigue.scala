package Information

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Performance.Cache
import Utilities.CountMap

trait Intrigue {
  def baseScouts(base: Base): Int = baseScoutMap(base)
  def baseIntrigue: Map[Base, Double] = baseIntrigueRaw.map(p => (p._1, p._2 / Math.pow(100.0, baseScouts(p._1))))
  def baseIntrigueRaw: Map[Base, Double] = cacheBaseIntrigueInitial()
  def mostBaselikeEnemyTile: Tile = mostBaselikeEnemyTileCache()

  def registerScout(base: Base): Unit = {
    baseScoutMap(base) += 1
  }

  protected def updateIntrigue(): Unit = {
    baseScoutMap.clear()
  }

  private val baseScoutMap = new CountMap[Base]

  private val cacheBaseIntrigueInitial = new Cache(() =>
    Maff
      .orElse(
        With.geography.bases.filter(b => ! b.owner.isUs),
        With.geography.bases)
      .map(base => (base, getBaseIntrigueInitial(base)))
      .toMap)

  private def getBaseIntrigueInitial(base: Base): Double = {
    val enemyHearts         = With.geography.enemyBases.map(_.heart)
    val heartMain           = base.heart.center
    val heartNatural        = base.natural.getOrElse(base).heart.center
    val hearts              = Vector(heartMain, heartNatural)
    val distanceFromEnemy   = 32.0 * 32.0 + Maff.min(enemyHearts.map(_.pixelDistanceGround(heartMain))).getOrElse(With.mapPixelWidth.toDouble)
    val informationAge      = 1.0 + With.framesSince(base.lastScoutedFrame)
    val startPositionBonus  = if (base.isStartLocation && base.lastScoutedFrame <= 0) 100.0 else 1.0
    val output              = startPositionBonus * informationAge / distanceFromEnemy
    output
  }

  private val mostBaselikeEnemyTileCache = new Cache(() =>
    With.units.enemy
      .view
      .filter(unit => unit.likelyStillThere && ! unit.flying && unit.unitClass.isBuilding)
      .toVector
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tile)
      .headOption
      .getOrElse(cacheBaseIntrigueInitial().maxBy(_._2)._1.townHallArea.midpoint))
}
