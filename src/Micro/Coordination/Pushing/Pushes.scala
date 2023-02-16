package Micro.Coordination.Pushing

import Information.Grids.ArrayTypes.GridBuffer
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsAll, IsVisible}
import bwapi.BulletType

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class Pushes {

  private var gridCurrent   : GridBuffer[Push] = new GridBuffer[Push]
  private var gridPrevious  : GridBuffer[Push] = new GridBuffer[Push]
  private var pushesCurrent : ArrayBuffer[Push] = new ArrayBuffer[Push]
  private var pushesPrevious : ArrayBuffer[Push] = new ArrayBuffer[Push]

  def put(push: Push): Unit = {
    pushesCurrent += push
    push.tiles.foreach(gridCurrent.addItem(push, _))
  }

  def all: Set[Push] = pushesPrevious.toSet ++ pushesCurrent

  def get(tile: Tile): Seq[Push] = gridCurrent.get(tile).view ++ gridPrevious.get(tile)

  def get(unit: UnitInfo): Seq[Push] = get(unit.tile)

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
        case BulletType.Gauss_Rifle_Hit     => checkMarineRange(bullet)
        case _ =>
      })

    With.units.all.foreach(unit => {
      if (unit.irradiated) {
        put(new ExplosionIrradiateSplash(unit))
      }
      if (Terran.SpiderMine(unit) && unit.visible && (unit.isEnemy || ! unit.burrowed && With.framesSince(unit.frameDiscovered) > Minutes(3)())) {
        put(new ExplosionSpiderMineBlast(unit))
      }
      if (Zerg.InfestedTerran(unit)) {
        put(new ExplosionInfestedTerran(unit))
      }
      if (Protoss.Scarab(unit) && unit.visible && ! unit.isOurs) {
        put(new ExplosionScarab(unit))
      }
      if (Terran.NuclearMissile(unit)) {
        put(new ExplosionNuke(unit.targetPixel.getOrElse(unit.pixel)))
      }
    })

    With.game.getNukeDots.asScala.view.map(new Pixel(_)).map(new ExplosionNuke(_)).foreach(put)
  }

  def checkMarineRange(bullet: BulletInfo): Unit = {
    if (bullet.sourceUnit.isEmpty
      && With.enemies.exists( ! Terran.MarineRange(_))
      && ! With.units.existsEnemy(IsAll(IsVisible, Terran.Marine))
      && bullet.targetUnit.exists(target =>
        target.matchups.threats.filter(Terran.Bunker).exists(_.pixelsToGetInRange(target) > 16))) {
      With.enemies.filter(_.isTerran).foreach(_.ratchetUpgradeLevel(Terran.MarineRange, 1))
    }
  }
}
