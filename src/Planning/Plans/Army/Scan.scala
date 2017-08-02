package Planning.Plans.Army

import Lifecycle.With
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
    // Meh.
    val cloakedThreats = With.units.enemy.filter(u =>
      u.effectivelyCloaked &&
      u.matchups.targets.nonEmpty)
    
    if (cloakedThreats.nonEmpty) {
      val biggestThreat = cloakedThreats.maxBy(_.matchups.vpfDealingDiffused)
      scanners.get.acquire(this)
      val units = scanners.get.units
      if (units.nonEmpty) {
        units.maxBy(_.energy).agent.intend(this, new Intention {
          toScan = Some(biggestThreat.pixelCenter)
        })
        lastScan = With.frame
      }
    }
  }
}
