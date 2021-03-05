package Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchAnd, MatchMobileFlying, MatchNot, MatchOr}

class DoFloatBuildings extends Prioritized {

  val floaties: LockUnits = new LockUnits
  floaties.counter.set(CountEverything)

  def update() {
    floaties.matcher.set(
      MatchAnd(
        MatchOr(With.blackboard.floatableBuildings(): _*),
        MatchNot(MatchMobileFlying)))
    floaties.acquire(this)
    floaties.units.foreach(floatie => {
      floatie.agent.intend(this, new Intention { canLiftoff = true })
    })
  }
}