package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Terran

class TacticScan extends Tactic {
  
  val scanners = new LockUnits(this, Terran.Comsat)
  var lastScan = 0
  
  def launch(): Unit = {
    if ( ! With.units.existsOurs(Terran.Comsat)) return
    if (With.framesSince(lastScan) < 72) return

    val scans = With.units.ours.filter(u => Terran.Comsat(u) && u.complete).map(_.energy / 50).sum

    if (scans == 0) {
      return
    }
    
    val cloakedTargets = With.units.enemy.filter(ninja =>
      ninja.effectivelyCloaked
      && (ninja.matchups.pixelsToTargetRange.exists(_ < 64) || scans > 1)
      && ninja.matchups.pixelsToThreatRange.exists(_ < 64))
    
    if (cloakedTargets.nonEmpty) {
      val biggestThreat = cloakedTargets.maxBy(t => t.subjectiveValue * (1 + t.matchups.dpfReceiving) * (1 + t.presumptiveTarget.map(_.subjectiveValue).getOrElse(0.0)))
      scan(biggestThreat.pixel)
      return
    }
    
    val tankTargets = With.units.enemy.filter(u =>
      ! u.visible
      && u.likelyStillThere
      && (u.unitClass.isStaticDefense || Terran.SiegeTankSieged(u))
      && u.matchups.threatDeepest.exists(t =>
        Terran.SiegeTankSieged(t)
        && t.inRangeToAttack(u)
        && t.readyForAttackOrder))
    val tankTarget = Maff.maxBy(tankTargets)(t => t.matchups.dpfReceiving)
  
    if (tankTarget.isDefined && scans > 1) {
      scan(tankTarget.get.pixel)
      return
    }
    
    val blockedBuilders = With.units.ours.filter(u =>
      u.intent.toBuild.exists(b => b.unitClass.isTownHall && u.pixelDistanceCenter(b.unitClass.tileArea.add(b.tile).center)  < 96)
      && u.seeminglyStuck)
    
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
