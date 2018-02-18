package Micro.Coordination

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Coordination.Explosions._
import ProxyBwapi.Bullets.BulletInfo
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
  
  def run() {
    byBattle.clear()
    With.bullets.all.foreach(explosionFromBullet)
    With.units.all.foreach(explosionFromUnit)
    With.game.getNukeDots.asScala.map(new Pixel(_)).foreach(explosionFromNuke)
  }
  
  def explosionFromBullet(bullet: BulletInfo) {
    val sourceUnit = bullet.sourceUnit
    if (sourceUnit.exists(_.is(Terran.ScienceVessel)))
      addToBattle(sourceUnit.get, new ExplosionEMP(bullet))
    else if (sourceUnit.exists(_.is(Zerg.Lurker)) && bullet.sourceUnit.get.isEnemy)
      addToBattle(sourceUnit.get, new ExplosionLurkerNow(bullet))
    else if (sourceUnit.exists(_.is(Protoss.HighTemplar)))
      addToBattle(sourceUnit.get, new ExplosionPsionicStorm(bullet))
  }
  
  def explosionFromUnit(unit: UnitInfo): Vector[Explosion] = {
    val output = new mutable.ArrayBuffer[Explosion]
    if (unit.irradiated)
      addToBattle(unit, new ExplosionIrradiateSplash(unit))
    if (unit.is(Terran.SpiderMine)) {
      addToBattle(unit, new ExplosionSpiderMineTrigger(unit))
      addToBattle(unit, new ExplosionSpiderMineBlast(unit))
    }
    if (unit.is(Zerg.InfestedTerran)) {
      addToBattle(unit, new ExplosionInfestedTerran(unit))
    }
    if (unit.is(Protoss.Scarab) && unit.visible && ! unit.isOurs) {
      addToNearbyUnits(unit.pixelCenter, new ExplosionScarab(unit))
    }
    if (unit.is(Zerg.Lurker)
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
    output.toVector
  }
  
  def explosionFromNuke(dot: Pixel) {
    addToNearbyUnits(dot, new ExplosionNuke(dot))
  }
}
