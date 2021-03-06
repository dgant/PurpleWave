package Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchAnd, MatchMobileFlying, MatchNot, MatchOr}

class DoFloatBuildings extends Prioritized {

  val floaties: LockUnits = new LockUnits(this)
  floaties.counter = CountEverything

  def update() {
    floaties.matcher = MatchAnd(
      MatchNot(MatchMobileFlying),
      MatchOr(With.blackboard.floatableBuildings(): _*))
    floaties.acquire(this)
    floaties.units.foreach(_.agent.intend(this, new Intention { canLiftoff = true }))
  }
}