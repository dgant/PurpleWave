package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.GetAtLeast
import Planning.Plans.Compound._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Matchup.WeAreZerg
import ProxyBwapi.Races.Zerg

class RequireEssentials extends Parallel(
  new Build(GetAtLeast(1, With.self.workerClass)),
  new RequireMiningBases(1),
  new If(
    new WeAreZerg,
    new Build(GetAtLeast(1, Zerg.Overlord))),
  new Build(GetAtLeast(3, With.self.workerClass))
)
