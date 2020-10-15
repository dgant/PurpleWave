package Micro.Coordination.Pushing

import Information.Fingerprinting.Generic.GameTime
import Information.Grids.ArrayTypes.GridItems
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.BulletType

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class Pushes {

  private var gridCurrent   : GridItems[Push] = new GridItems[Push]
  private var gridPrevious  : GridItems[Push] = new GridItems[Push]
  private var pushesCurrent : ArrayBuffer[Push] = new ArrayBuffer[Push]
  private var pushesPrevious : ArrayBuffer[Push] = new ArrayBuffer[Push]

  def put(push: Push): Unit = {
    pushesCurrent += push
    push.tiles.foreach(gridCurrent.addItem(push, _))
  }

  def all: Set[Push] = pushesPrevious.toSet ++ pushesCurrent

  def get(tile: Tile): Seq[Push] = {
    (gridCurrent.get(tile).view ++ gridPrevious.get(tile)).distinct
  }

  def get(unit: UnitInfo): Seq[Push] = {
    unit.tiles.flatMap(get).distinct
  }

  def onAgentCycle(): Unit = {
    val gridSwap = gridPrevious
    val pushesSwap = pushesPrevious

    gridPrevious = gridCurrent
    pushesPrevious = pushesCurrent

    gridCurrent = gridSwap
    pushesCurrent = pushesSwap

    gridCurrent.update()
    pushesCurrent.clear()

    addExplosions()
  }

  def addExplosions(): Unit = {
    // TODO:
    // - Disruption Web
    // - Dark Swarm
    // - Scourge
    // - Splash radii?

    With.bullets.all.foreach(bullet =>
      bullet.bulletType match {
        case BulletType.EMP_Missile         => put(new ExplosionEMP(bullet))
        case BulletType.Subterranean_Spines => if ( ! bullet.sourceUnit.exists(_.isOurs)) put(new ExplosionLurkerNow(bullet))
        case BulletType.Psionic_Storm       => put(new ExplosionPsionicStorm(bullet))
        case _ =>
      })

    With.units.all.foreach(unit => {
      if (unit.irradiated) {
        put(new ExplosionIrradiateSplash(unit))
      }
      if (unit.is(Terran.SpiderMine) && (unit.isEnemy || ! unit.burrowed && With.framesSince(unit.frameDiscovered) > GameTime(3, 0)())) {
        put(new ExplosionSpiderMineBlast(unit))
      }
      if (unit.is(Zerg.InfestedTerran)) {
        put(new ExplosionInfestedTerran(unit))
      }
      if (unit.is(Protoss.Scarab) && unit.visible && ! unit.isOurs) {
        put(new ExplosionScarab(unit))
      }
      if (unit.is(Terran.NuclearMissile)) {
        put(new ExplosionNuke(unit.targetPixel.getOrElse(unit.pixelCenter)))
      }
    })

    With.game.getNukeDots.asScala.foreach(dot => new ExplosionNuke(new Pixel(dot)))
  }
}
