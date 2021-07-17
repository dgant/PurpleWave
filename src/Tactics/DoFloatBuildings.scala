package Tactics

import Lifecycle.With
import Micro.Agency.Intention
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchAnd, MatchMobileFlying, MatchNot, MatchOr}

class DoFloatBuildings extends Tactic {

  val floaties: LockUnits = new LockUnits(this)
  floaties.counter = CountEverything

  def launch() {
    floaties.matcher = MatchAnd(
      MatchNot(MatchMobileFlying),
      MatchOr(With.blackboard.floatableBuildings(): _*))
    floaties.acquire(this)
    floaties.units.foreach(_.intend(this, new Intention { shouldLiftoff = true }))
  }
}