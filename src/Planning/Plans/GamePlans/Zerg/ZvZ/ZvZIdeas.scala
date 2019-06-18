package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Placement.BuildSunkensInMain
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Zerg

object ZvZIdeas {

  class ReactToFourPool extends Trigger(
    new EnemyStrategy(With.fingerprints.fourPool),
    new Parallel(
      new Trigger(
        new UnitsAtLeast(2, Zerg.SunkenColony),
        initialBefore = new CapGasWorkersAt(0)),
      new Pump(Zerg.Drone, 4),
      new Build(Get(Zerg.SpawningPool)),
      new Pump(Zerg.SunkenColony),
      new BuildSunkensInMain(2),
      new If(
        new UnitsAtLeast(2, Zerg.SunkenColony, complete = true),
        new Pump(Zerg.Drone, 9)),
      new PumpRatio(Zerg.Zergling, 0, 12, Seq(Enemy(Zerg.Zergling, 1.0))),
      new If(
        new UnitsAtLeast(8, Zerg.Drone, countEggs = true),
        new BuildSunkensInMain(4))
    ))
}
