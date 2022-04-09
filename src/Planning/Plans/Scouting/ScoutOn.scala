package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.If
import Planning.Predicates.Compound.Check
import Utilities.UnitFilters.UnitFilter

class ScoutOn(unitMatcher: UnitFilter, scoutCount: Int = 1, quantity: Int = 1) extends If(
  Check(() => With.units.ours.count(unitMatcher) >= quantity),
  new Write(With.blackboard.maximumScouts, () => Math.max(With.blackboard.maximumScouts(), scoutCount)))
