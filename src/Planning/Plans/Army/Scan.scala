package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.MatchTank
import ProxyBwapi.Races.Terran

class Scan extends Plan {
  
  val scanners = new LockUnits(this)
  scanners.matcher = Terran.Comsat
  
  var lastScan = 0
  
  override def onUpdate() {
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
      val biggestThreat = cloakedTargets.maxBy(_.matchups.vpfDealingInRange)
      scan(biggestThreat.pixel)
      return
    }
    
    val contentiousTanks = With.units.enemy.view.filter(u =>
      ! u.visible
      && u.likelyStillThere
      && u.is(MatchTank)
      &&u.matchups.enemies.exists(e =>
        e.is(Terran.SiegeTankSieged)
        && e.cooldownLeft == 0
        && e.inRangeToAttack(u)
        && ! e.matchups.targetsInRange.exists(_.visible)))
  
    if (contentiousTanks.nonEmpty) {
      scan(contentiousTanks.minBy(_.totalHealth).pixel)
      return
    }
    
    val blockedBuilders = With.units.ours.filter(b =>
      b.agent.toBuild.exists(_.isTownHall)
      && b.agent.toBuildTile.map(_.pixelCenter).exists(b.pixelDistanceCenter(_) < 96)
      && b.seeminglyStuck)
    
    if (blockedBuilders.nonEmpty) {
      scan(blockedBuilders.head.pixel)
      return
    }
  }
  
  def scan(targetPixel: Pixel) {
    scanners.acquire(this)
    val units = scanners.units
    if (units.nonEmpty) {
      units.maxBy(_.energy).agent.intend(this, new Intention { toScan = Some(targetPixel) })
      lastScan = With.frame
    }
  }
}
