package Planning.Plans.Army

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Plan
import ProxyBwapi.Races.Terran

class Scan extends Plan {
  
  description.set("Use Scanner Sweep")
  
  val scanners = new Property[LockUnits](new LockUnits { unitMatcher.set(Terran.Comsat) })
  
  var lastScan = 0
  
  override def onUpdate() {
    if (With.framesSince(lastScan) < 72) {
      return
    }
    
    // TODO: Actually check whether we can attack the darned thing.
    val cloakedTargets = With.units.enemy.filter(ninja =>
      ninja.effectivelyCloaked
      && ninja.matchups.targets.nonEmpty
      && ninja.matchups.enemies.exists(defender =>
        (if (ninja.flying) defender.unitClass.attacksAir else defender.unitClass.attacksGround)
        && defender.inRangeToAttack(ninja)))
    
    if (cloakedTargets.nonEmpty) {
      val biggestThreat = cloakedTargets.maxBy(_.matchups.vpfDealingDiffused)
      scan(biggestThreat.pixelCenter)
      return
    }
    
    val contentiousTanks = With.units.enemy.filter(u =>
      ! u.visible &&
      u.likelyStillThere &&
      u.unitClass.isSiegeTank &&
      u.matchups.enemies.exists(e =>
        e.is(Terran.SiegeTankSieged)  &&
        e.cooldownLeft == 0           &&
        e.inRangeToAttack(u)      &&
        ! e.matchups.targetsInRange.exists(_.visible)))
  
    if (contentiousTanks.nonEmpty) {
      scan(contentiousTanks.minBy(_.totalHealth).pixelCenter)
      return
    }
    
    val blockedBuilders = With.units.ours.filter(b =>
      b.agent.toBuild.exists(_.isTownHall)
      && With.framesSince(b.lastFrameTryingToMove) > GameTime(0, 1)())
    
    if (blockedBuilders.nonEmpty) {
      scan(blockedBuilders.head.pixelCenter)
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
