package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Compound.{Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.EnemyHasShownCloakedThreat
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Protoss, Terran}

object TvPIdeas {
  
  def workerPlan: Plan = new Parallel(
    new TrainContinuously(Terran.Comsat),
    new TrainWorkersContinuously)
  
  def emergencyPlans: Seq[Plan] =
    Vector(
      new If(
        new Check(() =>
          10 * With.units.countEnemy(Protoss.Carrier)
          > With.units.countOurs(Terran.Marine)
          + 3 * With.units.countOurs(Terran.Goliath)),
        new Parallel(
          new UpgradeContinuously(Terran.GoliathAirRange),
          new TrainContinuously(Terran.Goliath),
          new TrainContinuously(Terran.Marine),
          new Build(Get(1, Terran.Armory)))),
      new If(
        new EnemyHasShownCloakedThreat,
        new Parallel(
          new TrainContinuously(Terran.Comsat),
          new TrainContinuously(Terran.ScienceVessel, 2, 1),
          new Build(
            Get(1, Terran.EngineeringBay),
            Get(3, Terran.MissileTurret),
            Get(1, Terran.Academy)))))
}
