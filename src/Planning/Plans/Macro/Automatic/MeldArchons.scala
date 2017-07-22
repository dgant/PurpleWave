package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchEnergyAtMost, UnitMatchType}
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons(maxEnergy: Int = 200) extends Plan {
  
  val templar = new LockUnits
  templar.unitMatcher.set(
    UnitMatchAnd(
      UnitMatchType(Protoss.HighTemplar),
      UnitMatchEnergyAtMost(maxEnergy)))
  
  override def onUpdate() {
    templar.acquire(this)
    templar.units.foreach(_.intend(new Intention(this) {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
