package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchOr
import ProxyBwapi.Races.Terran

class FloatBuildings extends Plan {

  val floaties: LockUnits = new LockUnits {
    unitMatcher.set(UnitMatchOr(Terran.Barracks, Terran.EngineeringBay))
    unitCounter.set(UnitCountEverything)
  }

  override def onUpdate() {
    floaties.acquire(this)
    floaties.units.foreach(floatie => {
      if (floatie.flying) {
        With.squads.addFreelancer(floatie)
      }
      else {
        floatie.agent.intend(this, new Intention {
          canLiftoff = true
        })
      }
    })

  }
}