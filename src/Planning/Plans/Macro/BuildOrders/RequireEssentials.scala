package Planning.Plans.Macro.BuildOrders

import Lifecycle.With
import Macro.Buildables.Get
import Planning.Plans.Compound._
import Planning.Plans.Macro.Expanding.{MaintainMiningBases, RequireMiningBases}
import Planning.Predicates.Strategy.WeAreZerg
import ProxyBwapi.Races.Zerg

class RequireEssentials extends Parallel(
  new Build(Get(1, With.self.workerClass)),
  new RequireMiningBases(1),
  new If(
    new WeAreZerg,
    new Build(Get(1, Zerg.Overlord))),
  new Build(Get(3, With.self.workerClass)),
  new MaintainMiningBases
)
