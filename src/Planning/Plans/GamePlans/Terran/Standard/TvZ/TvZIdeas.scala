package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.CancelAll
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Terran.BuildBunkersAtMain
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.UnitsAtMost
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Terran

object TvZIdeas {

  class TvZFourPoolEmergency extends If(
    new EnemyStrategy(With.fingerprints.fourPool),
    new Parallel(
      new If(
        new And(
          new UnitsAtMost(0, Terran.Bunker),
          new UnitsAtMost(1, Terran.Barracks)),
        new CancelAll(Terran.SupplyDepot, Terran.Refinery)),
      new Pump(Terran.SCV, 5),
      new Build(Get(1, Terran.Barracks)),
      new Pump(Terran.Marine, 4),
      new BuildBunkersAtMain(1),
      new Pump(Terran.SCV, 6),
      new Build(Get(Terran.SupplyDepot, 1)),
      new Pump(Terran.SCV, 14),
      new Pump(Terran.Marine, 8),
      new Build(Get(2, Terran.Barracks))))
}
