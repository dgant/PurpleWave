package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Matchup.WeAreZerg
import ProxyBwapi.Races.Zerg

class RequireEssentials extends Parallel(
  new Build(RequestAtLeast(1, With.self.workerClass)),
  new RequireMiningBases(1),
  new If(
    new WeAreZerg,
    new Build(RequestAtLeast(1, Zerg.Overlord))),
  new Build(RequestAtLeast(3, With.self.workerClass))
)
