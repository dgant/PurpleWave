package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Plans.Basic.Do
import Planning.Plans.Compound.If
import Planning.Predicates.Milestones.SupplyOutOf200

class ScoutAt(minimumSupply: Int, maxScouts: Int = 1) extends If(
  new SupplyOutOf200(minimumSupply),
  new Do(() => With.blackboard.maximumScouts.set(Math.max(With.blackboard.maximumScouts(), maxScouts))))
