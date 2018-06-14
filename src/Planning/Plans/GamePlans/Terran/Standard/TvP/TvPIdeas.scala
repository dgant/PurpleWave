package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Compound.{Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.EnemyHasShownCloakedThreat
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Protoss, Terran}

object TvPIdeas {
  
  def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers)
  
  def emergencyPlans: Seq[Plan] =
    Vector(
      new If(
        new Check(() =>
          10 * With.units.countEnemy(Protoss.Carrier)
          > With.units.countOurs(Terran.Marine)
          + 3 * With.units.countOurs(Terran.Goliath)),
        new Parallel(
          new UpgradeContinuously(Terran.GoliathAirRange),
          new Pump(Terran.Goliath),
          new Pump(Terran.Marine),
          new Build(Get(1, Terran.Armory)))),
      new If(
        new EnemyHasShownCloakedThreat,
        new Parallel(
          new Pump(Terran.Comsat),
          new Pump(Terran.ScienceVessel, 2, 1),
          new Build(
            Get(1, Terran.EngineeringBay),
            Get(3, Terran.MissileTurret),
            Get(1, Terran.Academy)))))
}
