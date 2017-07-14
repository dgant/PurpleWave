package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons extends Plan {
  
  val templar = new LockUnits
  templar.unitMatcher.set(UnitMatchType(Protoss.HighTemplar))
  
  override def onUpdate() {
    templar.acquire(this)
    templar.units.foreach(unit => With.executor.intend(
      new Intention(this, unit) {
        toTravel = Some(With.geography.home.pixelCenter)
        canMeld = true
      }
    ))
  }
}
