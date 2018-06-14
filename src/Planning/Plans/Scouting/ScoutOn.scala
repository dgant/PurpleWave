package Planning.Plans.Scouting

import Planning.UnitMatchers.UnitMatcher
import Planning.Plans.Compound.If
import Planning.Predicates.Milestones.UnitsAtLeast

class ScoutOn(unitMatcher: UnitMatcher, scoutCount: Int = 1, quantity: Int = 1) extends If(
  new UnitsAtLeast(quantity, unitMatcher, complete = false),
  new Scout(scoutCount)) {
  
  description.set("Scout on " + unitMatcher)
}
