package Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.MatchOr

class DoFloatBuildings extends Prioritized {

  val floaties: LockUnits = new LockUnits
  floaties.counter.set(CountEverything)

  def update() {
    floaties.matcher.set(MatchOr(With.blackboard.floatableBuildings(): _*))
    floaties.acquire(this)
    floaties.units.foreach(floatie => {
      if (floatie.flying) {
        With.squads.freelance(floatie)
      } else {
        floatie.agent.intend(this, new Intention { canLiftoff = true })
      }
    })
  }
}