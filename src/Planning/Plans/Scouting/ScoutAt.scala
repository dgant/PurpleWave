package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.If
import Planning.Predicates.Milestones.SupplyOutOf200

class ScoutAt(minimumSupply: Int, maxScouts: Int = 1) extends If(
  SupplyOutOf200(minimumSupply),
  new Write(With.blackboard.maximumScouts, () => Math.max(With.blackboard.maximumScouts(), maxScouts)))
