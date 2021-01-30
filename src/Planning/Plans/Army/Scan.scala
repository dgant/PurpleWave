package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchSiegeTank
import Planning.{Plan, Property}
import ProxyBwapi.Races.Terran
import Utilities.Seconds

class Scan extends Plan {
  
  description.set("Use Scanner Sweep")
  
  val scanners = new Property[LockUnits](new LockUnits { unitMatcher.set(Terran.Comsat) })
  
  var lastScan = 0
  
  override def onUpdate() {
    if (With.units.countOurs(Terran.Comsat) == 0) {
      return
    }
    if (With.framesSince(lastScan) < 72) {
      return
    }
    
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
      && u.is(UnitMatchSiegeTank)
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
      && b.framesFailingToMove > Seconds(1)())
    
    if (blockedBuilders.nonEmpty) {
      scan(blockedBuilders.head.pixel)
      return
    }
  }
  
  def scan(targetPixel: Pixel) {
    scanners.get.acquire(this)
    val units = scanners.get.units
    if (units.nonEmpty) {
      units.maxBy(_.energy).agent.intend(this, new Intention {
        toScan = Some(targetPixel)
      })
      lastScan = With.frame
    }
  }
}
