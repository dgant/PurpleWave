package Planning.Plans.Gameplans.Zerg.ZvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Placement.BuildSunkensAtNatural
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.{GasForUpgrade, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.{Terran, Zerg}

object ZvTIdeas {

  class ReactToBarracksCheese extends If(
    new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs),
    new Parallel(
      new If(
        new And(
          new GasForUpgrade(Zerg.ZerglingSpeed),
          new UnitsAtMost(1, Zerg.Hatchery)),
        new CapGasWorkersAt(0)),
      new BuildOrder(
        Get(9, Zerg.Drone),
        Get(Zerg.SpawningPool),
        Get(10, Zerg.Drone),
        Get(Zerg.Extractor),
        Get(2, Zerg.Overlord),
        Get(11, Zerg.Drone),
        Get(6, Zerg.Zergling),
        Get(Zerg.ZerglingSpeed)),
      new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(Terran.Marine, 2.0))),
      new Pump(Zerg.Drone, 11),
      new RequireBases(2),
      new Pump(Zerg.SunkenColony),
      new If(
        new UnitsAtLeast(2, Zerg.Hatchery, complete = true),
        new BuildSunkensAtNatural(1))))
}
