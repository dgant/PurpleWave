package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldDarkArchons extends Plan {
  
  val templar = new LockUnits
  templar.unitMatcher.set(Protoss.DarkTemplar)
  
  override def onUpdate() {
    templar.acquire(this)
    templar.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
