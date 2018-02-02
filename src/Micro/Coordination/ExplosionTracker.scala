package Micro.Coordination

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Coordination.Explosions._
import ProxyBwapi.Bullets.BulletInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.JavaConverters._
import scala.collection.mutable

class ExplosionTracker {
  
  var all: Vector[Explosion] = Vector.empty
  
  def run() {
    val bullets = With.bullets.all.flatMap(explosionFromBullet)
    val units = With.units.all.flatMap(explosionFromUnit)
    val nukes = With.game.getNukeDots.asScala.map(new Pixel(_)).map(explosionFromNuke)
    // TODO: Shoves
    all = Vector.empty ++ bullets ++ units ++ nukes
  }
  
  def explosionFromBullet(bullet: BulletInfo): Option[Explosion] = {
    if (bullet.sourceUnit.exists(_.is(Terran.ScienceVessel)))
      Some(new ExplosionEMP(bullet))
    else if (bullet.sourceUnit.exists(_.is(Zerg.Lurker)))
      Some(new ExplosionLurker(bullet))
    else if (bullet.sourceUnit.exists(_.is(Protoss.HighTemplar)))
      Some(new ExplosionPsionicStorm(bullet))
    else
      None
  }
  
  def explosionFromUnit(unit: UnitInfo): Vector[Explosion] = {
    val output = new mutable.ArrayBuffer[Explosion]
    // TODO -- add irradiate property
    /*
    if (unit.irradiated) {
      output += new ExplosionIrradiate()(unit)
    }
    */
    if (unit.is(Terran.SpiderMine)) {
      output += new ExplosionSpiderMineTrigger(unit)
      output += new ExplosionSpiderMineBlast(unit)
    }
    if (unit.is(Zerg.InfestedTerran) && unit.visible) {
      output += new ExplosionInfestedTerran(unit)
    }
    if (unit.is(Protoss.Scarab) && unit.visible) {
      output += new ExplosionScarab(unit)
    }
    output.toVector
  }
  
  def explosionFromNuke(dot: Pixel): Explosion = {
    new ExplosionNuke(dot)
  }
}
