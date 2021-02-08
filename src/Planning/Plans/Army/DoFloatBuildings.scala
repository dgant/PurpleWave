package Planning.Plans.Army

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchOr

class DoFloatBuildings extends Prioritized {

  val floaties: LockUnits = new LockUnits
  floaties.unitCounter.set(UnitCountEverything)
  def update() {
    floaties.unitMatcher.set(UnitMatchOr(With.blackboard.floatableBuildings(): _*))
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