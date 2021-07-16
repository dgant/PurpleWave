package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Do
import Planning.Plans.Compound.If
import Planning.Predicates.Compound.Check
import Planning.UnitMatchers.UnitMatcher

class ScoutOn(unitMatcher: UnitMatcher, scoutCount: Int = 1, quantity: Int = 1) extends If(
  Check(() =>
    With.units.ours.count(u =>
      unitMatcher(u)
      && u.getProducer.forall(With.recruiter.lockedBy(_).isEmpty)) // Make sure we can scout with the builder
      >= quantity),
  new Do(() => With.blackboard.maximumScouts.set(Math.max(With.blackboard.maximumScouts(), scoutCount))))
