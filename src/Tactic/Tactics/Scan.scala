package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Terran
import Utilities.UnitFilters.IsTank

class Scan extends Tactic {
  
  val scanners = new LockUnits(this, Terran.Comsat)
  var lastScan = 0
  
  def launch(): Unit = {
    if ( ! With.units.existsOurs(Terran.Comsat)) return
    if (With.framesSince(lastScan) < 72) return
    
    // TODO: Actually check whether we can attack the darned thing.
    val cloakedTargets = With.units.enemy.view.filter(ninja =>
      ninja.effectivelyCloaked
      && ninja.matchups.targets.nonEmpty
      && ninja.matchups.enemies.exists(defender =>
        (if (ninja.flying) defender.unitClass.attacksAir else defender.unitClass.attacksGround)
        && defender.inRangeToAttack(ninja)))
    
    if (cloakedTargets.nonEmpty) {
      val biggestThreat = cloakedTargets.maxBy(t => Maff.max(t.matchups.targetsInRange.map(_.subjectiveValue)).getOrElse(0.0))
      scan(biggestThreat.pixel)
      return
    }
    
    val contentiousTanks = With.units.enemy.view.filter(u =>
      ! u.visible
      && u.likelyStillThere
      && u.is(IsTank)
      && u.matchups.enemies.exists(e =>
        Terran.SiegeTankSieged(e)
        && e.cooldownLeft == 0
        && e.inRangeToAttack(u)
        && ! e.matchups.targetsInRange.exists(_.visible)))
  
    if (contentiousTanks.nonEmpty) {
      scan(contentiousTanks.minBy(_.totalHealth).pixel)
      return
    }
    
    val blockedBuilders = With.units.ours.filter(b =>
      b.intent.toBuild.exists(_.isTownHall)
      && b.intent.toBuildTile.map(_.center).exists(b.pixelDistanceCenter(_) < 96)
      && b.seeminglyStuck)
    
    if (blockedBuilders.nonEmpty) {
      scan(blockedBuilders.head.pixel)
      return
    }
  }
  
  def scan(targetPixel: Pixel): Unit = {
    Maff.maxBy(scanners.acquire())(_.energy).foreach(scanner => {
      scanner.intend(this).setScan(targetPixel)
      lastScan = With.frame
    })
  }
}
