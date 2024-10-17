package Planning.Plans.Gameplans.Terran.TvZ

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Cancel
import Planning.Plans.Placement.{BuildBunkersAtMain, BuildBunkersAtNatural}
import Planning.Predicates.Compound.{And, Sticky}
import Planning.Predicates.Economy.MineralsAtMost
import Planning.Predicates.Milestones.{FrameAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

object TvZIdeas {

  class TvZFourPoolEmergency extends If(
    new EnemyStrategy(With.fingerprints.fourPool),
    new Parallel(
      new If(
        new And(
          new UnitsAtMost(0, Terran.Bunker),
          new UnitsAtMost(1, Terran.Barracks)),
        new Cancel(Terran.SupplyDepot, Terran.Refinery)),
      new If(
        new And(
          new UnitsAtMost(0, Terran.Bunker),
          new UnitsAtLeast(1, Terran.Barracks, complete = true),
          new MineralsAtMost(91)),
        new Cancel(Terran.SupplyDepot, Terran.Barracks)),
      new Pump(Terran.SCV, 5),
      new Build(Get(Terran.Barracks)),
      new Pump(Terran.Marine, 4),
      new BuildBunkersAtMain(1),
      new Pump(Terran.SCV, 6),
      new Build(Get(Terran.SupplyDepot)),
      new Pump(Terran.SCV, 14),
      new Pump(Terran.Marine, 8),
      new Build(Get(2, Terran.Barracks))))


  class TvZ1RaxExpandVs9Pool extends If(
    new EnemyStrategy(With.fingerprints.ninePool, With.fingerprints.overpool),
    new If(
      new Sticky(new FrameAtLeast(GameTime(2, 50)())),
      new BuildBunkersAtNatural(1),
      new Parallel(
        new BuildBunkersAtMain(1),
        new Build(Get(2, Terran.Barracks)),
        new BuildBunkersAtNatural(1))))
}
