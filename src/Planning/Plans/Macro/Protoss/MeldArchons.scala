package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitPreferences.PreferLowEnergy
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons(maxEnergy: Int = 251) extends Plan {
  
  protected def maximumTemplar: Int = 100
  protected def minimumArchons: Int = 0
  
  val templar = new LockUnits(this)
  templar.matcher = Protoss.HighTemplar
  templar.preference = PreferLowEnergy
  
  override def onUpdate(): Unit = {
    // Fast check for performance
    val proceed = With.self.isProtoss && With.units.existsOurs(Protoss.HighTemplar)
    if ( ! proceed) return

    val templarNow    = With.units.countOurs(Protoss.HighTemplar)
    val templarLow    = With.units.countOursP(u => u.is(Protoss.HighTemplar) && u.energy < maxEnergy)
    val templarExcess = templarNow - maximumTemplar
    val archonsNow    = With.units.countOurs(Protoss.Archon)
    val archonsToAdd  = Vector(0, minimumArchons - archonsNow, templarExcess / 2).max
    val templarToMeld = Math.max(templarLow, 2 * archonsToAdd)
    templar.counter = CountUpTo(templarToMeld)
    
    templar.release()
    templar.acquire()
    templar.units.foreach(_.intend(this, new Intention {
      toTravel = Some(With.geography.home.center)
      shouldMeld = true
    }))
  }
}
