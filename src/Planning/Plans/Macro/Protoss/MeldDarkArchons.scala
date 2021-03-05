package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldDarkArchons extends Plan {
  
  val templar = new LockUnits
  templar.matcher = Protoss.DarkTemplar
  
  override def onUpdate() {
    templar.release() //We don't want them if they're Dark Archons now.
    templar.acquire(this)
    templar.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
