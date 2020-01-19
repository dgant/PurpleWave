package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.{UnitMatchOr, UnitMatcher}
import ProxyBwapi.Races.Terran

class FloatBuildings(val toFloat: UnitMatcher = UnitMatchOr(Terran.Barracks, Terran.EngineeringBay)) extends Plan {

  val floaties: LockUnits = new LockUnits {
    unitMatcher.set(toFloat)
    unitCounter.set(UnitCountEverything)
  }

  override def onUpdate() {
    floaties.acquire(this)
    floaties.units.foreach(floatie => {
      if (floatie.flying) {
        With.squads.freelance(floatie)
      }
      else {
        floatie.agent.intend(this, new Intention {
          canLiftoff = true
        })
      }
    })

  }
}