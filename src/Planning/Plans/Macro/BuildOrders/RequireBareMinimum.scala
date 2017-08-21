package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound.Parallel
import Planning.Plans.Macro.Expanding.RequireMiningBases

class RequireBareMinimum extends Parallel(
  new Build(
    RequestAtLeast(1, With.self.workerClass),
    RequestAtLeast(1, With.self.townHallClass)
  ),
  new RequireMiningBases(1)
)
