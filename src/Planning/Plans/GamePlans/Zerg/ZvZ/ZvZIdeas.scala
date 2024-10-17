package Planning.Plans.Gameplans.Zerg.ZvZ

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.{FrameAtMost, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Placement.BuildSunkensInMain
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Zerg
import Utilities.Time.GameTime

object ZvZIdeas {

  class ReactToFourPool extends If(
    new And(
      new EnemyStrategy(With.fingerprints.fourPool),
      new UnitsAtMost(3, Zerg.SunkenColony, complete = true),
      new UnitsAtMost(1, Zerg.Hatchery, complete = true),
      new FrameAtMost(GameTime(4, 0)())),
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
        new UnitsAtLeast(8, Zerg.Drone),
        new BuildSunkensInMain(4))
    ))
}
