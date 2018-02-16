package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Compound._
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Matchup.WeAreZerg
import Planning.Plans.Predicates.Milestones.UnitsAtMost
import ProxyBwapi.Races.Zerg

class RequireEssentials extends Parallel(
  new Build(RequestAtLeast(1, With.self.workerClass)),
  new If(
    new Or(
      new Not(new WeAreZerg),
      new And(
        new UnitsAtMost(0, Zerg.Hatchery),
        new UnitsAtMost(0, Zerg.Lair),
        new UnitsAtMost(0, Zerg.Hive))),
    new Build(RequestAtLeast(1, With.self.townHallClass))),
  new RequireMiningBases(1)
)
