package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountBetween
import Planning.Composition.UnitPreferences.UnitPreferLowEnergy
import Planning.Plan
import ProxyBwapi.Races.Protoss

class MeldArchons(maxEnergy: Int = 250) extends Plan {
  
  protected def minimumArchons: Int = 0
  
  val templar = new LockUnits
  templar.unitMatcher.set(Protoss.HighTemplar)
  templar.unitPreference.set(UnitPreferLowEnergy)
  
  override def onUpdate() {
    val templarLow    = With.units.ours.count(u => u.is(Protoss.HighTemplar) && u.energy < maxEnergy)
    val archonsNow    = With.units.ours.count(_.is(Protoss.Archon))
    val archonsToAdd  = Math.max(0, minimumArchons - archonsNow)
    val templarToMeld = Math.max(templarLow, 2 * archonsToAdd)
    templar.unitCounter.set(new UnitCountBetween(0, templarToMeld))
    
    templar.release()
    templar.acquire(this)
    templar.units.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(With.geography.home.pixelCenter)
      canMeld = true
    }))
  }
}
