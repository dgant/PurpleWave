package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchEnergyAtMost}
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons(maxEnergy: Int = 200) extends Plan {
  
  val templar = new LockUnits
  templar.unitMatcher.set(UnitMatchAnd(Protoss.HighTemplar, UnitMatchEnergyAtMost(maxEnergy)))
  
  override def onUpdate() {
    templar.release()
    templar.acquire(this)
    templar.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
