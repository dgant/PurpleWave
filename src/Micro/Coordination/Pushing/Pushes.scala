package Micro.Coordination.Pushing

import Information.Grids.ArrayTypes.GridItems
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.BulletType

import scala.collection.JavaConverters._

class Pushes {

  private var current   : GridItems[Push] = new GridItems[Push]
  private var previous  : GridItems[Push] = new GridItems[Push]

  def put(push: Push): Unit = {
    push.tiles.foreach(current.addItem(push, _))
  }

  def get(tile: Tile): Seq[Push] = {
    (current.get(tile).view ++ previous.get(tile)).distinct
  }

  def get(unit: UnitInfo): Seq[Push] = {
    unit.tiles.flatMap(get).distinct
  }

  def onAgentCycle(): Unit = {
    val swap = previous
    previous = current
    current = swap
    current.update()
  }

  def addExplosions(): Unit = {
    With.bullets.all.foreach(bullet =>
      bullet.bulletType match {
        case BulletType.EMP_Missile         => put(new ExplosionEMP(bullet))
        case BulletType.Subterranean_Spines => if ( ! bullet.sourceUnit.exists(_.isOurs)) put(new ExplosionLurkerNow(bullet))
        case BulletType.Psionic_Storm       => put(new ExplosionPsionicStorm(bullet))
      })

    With.units.all.foreach(unit =>
      if (unit.irradiated) {
        put(new ExplosionIrradiateSplash(unit))
      } else if (unit.is(Terran.SpiderMine)
        && ( ! unit.burrowed || unit.matchups.enemies.exists(e => e.unitClass.triggersSpiderMines && e.pixelDistanceEdge(unit) < 64))
        && unit.isFriendly) {
        put(new ExplosionSpiderMineBlast(unit))
      } else if (unit.is(Zerg.InfestedTerran)) {
        put(new ExplosionInfestedTerran(unit))
      } else if (unit.is(Protoss.Scarab) && unit.visible && ! unit.isOurs) {
        put(new ExplosionScarab(unit))
      } else if (unit.is(Terran.NuclearMissile)) {
        put(new ExplosionNuke(unit.targetPixel.getOrElse(unit.pixelCenter)))
      })

    With.game.getNukeDots.asScala.foreach(dot => new ExplosionNuke(new Pixel(dot)))
  }
}
