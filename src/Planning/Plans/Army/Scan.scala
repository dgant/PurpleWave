package Planning.Plans.Army

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import ProxyBwapi.Races.Terran

class Scan extends Plan {
  
  description.set("Use Scanner Sweep")
  
  val scanners = new Property[LockUnits](new LockUnits {
    unitMatcher.set(UnitMatchType(Terran.Comsat))
  })
  
  var lastScan = 0
  
  override def onUpdate() {
    if (With.framesSince(lastScan) < 72) {
      return
    }
    
    // TODO: Actually check whether we can attack the darned thing.
    val cloakedThreats = With.units.enemy.filter(u =>
      u.effectivelyCloaked &&
      u.matchups.targets.nonEmpty)
    
    if (cloakedThreats.nonEmpty) {
      val biggestThreat = cloakedThreats.maxBy(_.matchups.vpfDealingDiffused)
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
        e.inRangeToAttackFast(u)      &&
        ! e.matchups.targetsInRange.exists(_.visible)))
  
    if (contentiousTanks.nonEmpty) {
      scan(contentiousTanks.minBy(_.totalHealth).pixelCenter)
      return
    }
    
    val blockedBuilders = With.units.ours.filter(b =>
      b.agent.toBuild.exists(_.isTownHall)
      && With.framesSince(b.lastMovementFrame) > 24 )
    
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
