package Planning.Plans.Macro.Protoss

import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitPreferences.PreferLowEnergy

class MeldArchons(maxEnergy: Int = 251) extends Plan {
  
  protected def maximumTemplar: Int = 100
  protected def minimumArchons: Int = 0
  
  val templar = new LockUnits(this, Protoss.HighTemplar, PreferLowEnergy)
  
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

    templar
      .setCounter(CountUpTo(templarToMeld))
      .release()
      .acquire()
      .foreach(_.intend(this).setShouldMeld(true).setTravel(With.geography.home.center))
  }
}
