package Micro.Coordination

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Coordination.Explosions._
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ExplosionTracker {
  
  def all: Set[Explosion] = byBattle.values.flatten.toSet
  private val byBattle: mutable.HashMap[Battle, ArrayBuffer[Explosion]] = mutable.HashMap.empty
  
  def nearUnit(unit: UnitInfo): Iterable[Explosion] = {
    if (unit.battle.isDefined)
      byBattle.getOrElse(unit.battle.get, Iterable.empty)
    else
      Iterable.empty
  }
  
  protected def addToBattle(unit: UnitInfo, explosion: Explosion) {
    unit.battle.foreach(addToBattle(_, explosion))
  }
  
  protected def addToBattle(battle: Battle, explosion: Explosion) {
    byBattle(battle) = byBattle.getOrElse(battle, ArrayBuffer.empty)
    byBattle(battle) += explosion
  }
  
  protected def addToNearbyUnits(pixel: Pixel, explosion: Explosion) {
    val units = With.units.inTileRadius(pixel.tileIncluding, 8)
    val battles = units.flatMap(_.battle).toSet
    battles.foreach(addToBattle(_, explosion))
  }
  
  def onAgentCycle() {
    byBattle.clear()
    With.bullets.all.foreach(explosionFromBullet)
    With.units.all.foreach(explosionFromUnit)
    With.game.getNukeDots.asScala.map(new Pixel(_)).foreach(explosionFromNuke)
  }
  
  def explosionFromBullet(bullet: BulletInfo) {
    lazy val someTerran   = Players.all.exists(_.isTerran)
    lazy val someProtoss  = Players.all.exists(_.isProtoss)
    lazy val someZerg     = Players.all.exists(_.isZerg)
    lazy val sourceUnit   = bullet.sourceUnit
    if (someTerran && sourceUnit.exists(_.is(Terran.ScienceVessel)))
      addToBattle(sourceUnit.get, new ExplosionEMP(bullet))
    else if (someZerg && sourceUnit.exists(u => u.is(Zerg.Lurker) && u.isEnemy && u.effectivelyCloaked)) {
      addToBattle(sourceUnit.get, new ExplosionLurkerNow(bullet))
    }
    else if (someProtoss && sourceUnit.exists(_.is(Protoss.HighTemplar))) {
      addToBattle(sourceUnit.get, new ExplosionPsionicStorm(bullet))
    }
  }
  
  def explosionFromUnit(unit: UnitInfo): Seq[Explosion] = {
    val output = new mutable.ArrayBuffer[Explosion]
    if (unit.irradiated) {
      addToBattle(unit, new ExplosionIrradiateSplash(unit))
    }
    else if (unit.is(Terran.SpiderMine)
      && ( ! unit.burrowed || unit.matchups.enemies.exists(e => e.unitClass.triggersSpiderMines && e.pixelDistanceEdge(unit) < 64))
      && unit.isFriendly) {
      addToBattle(unit, new ExplosionSpiderMineBlast(unit))
    }
    else if (unit.is(Zerg.InfestedTerran)) {
      addToBattle(unit, new ExplosionInfestedTerran(unit))
    }
    else if (unit.is(Protoss.Scarab) && unit.visible && ! unit.isOurs) {
      addToNearbyUnits(unit.pixelCenter, new ExplosionScarab(unit))
    }
    else if (unit.is(Terran.NuclearMissile)) {
      addToNearbyUnits(unit.pixelCenter, new ExplosionNuke(unit.targetPixel.getOrElse(unit.pixelCenter)))
    }
    else if (unit.is(Zerg.Lurker)
      && ! unit.isOurs
      && unit.burrowed
      && With.grids.friendlyVision.isSet(unit.tileIncludingCenter)
      && unit.battle.isDefined
      && unit.matchups.targets.exists(_.pixelDistanceEdge(unit) < 32 * 8)) {
      // TODO:
      // * Don't do it if the Lurker isn't about to fire
      // * Don't do it if we're trying to attack the Lurker and have shorter range
      // * Associate these explosions with the unit so we don't spam-check this explosion for the whole map
      // unit.matchups.targetsInRange.foreach(target => output += new ExplosionLurkerSoon(unit, target))
    }
    output
  }
  
  def explosionFromNuke(dot: Pixel) {
    addToNearbyUnits(dot, new ExplosionNuke(dot))
  }
}
