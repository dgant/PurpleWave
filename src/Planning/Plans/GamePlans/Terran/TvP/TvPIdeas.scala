package Planning.Plans.GamePlans.Terran.TvP

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.{AttackAndHarass, ConsiderAttacking}
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.{And, Check, Not, Or}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeToMoveOut
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP2FacJoyO

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

  class TvPAttack extends Parallel(
    // Keep pressuring if appropriate
    new If(
      new Employing(TvP2FacJoyO),
      new ConsiderAttacking,
      // Otherwise, wait for our later timing
      new Trigger(
        new Not(new SafeToMoveOut),
        initialBefore = new AttackAndHarass)),
    new Trigger(
      new Or(
        new MiningBasesAtLeast(3),
        new UpgradeComplete(Terran.MechDamage, 2),
        new EnemyHasShown(Protoss.Carrier),
        new EnemyHasShown(Protoss.Interceptor),
        new EnemyHasShown(Protoss.FleetBeacon)),
      new AttackAndHarass))

  class CutGasDuringFactory extends If(
    new And(
      new UnitsAtMost(0, Terran.Factory, complete = true),
      new Or(
        new GasAtLeast(100),
        new UnitsAtLeast(1, Terran.Factory))),
    new CapGasWorkersAt(1))

  class ReactiveDetection extends If(
    new And(
      new Or(
        new MiningBasesAtLeast(3),
        new EnemyHasShown(Protoss.DarkTemplar, 1),
        new EnemyHasShown(Protoss.Arbiter),
        new EnemyHasShown(Protoss.ArbiterTribunal)),
      new UnitsAtLeast(2, Terran.Factory)),
    new Parallel(
      new Build(
        Get(Terran.Academy),
        Get(Terran.Starport),
        Get(Terran.ScienceFacility),
        Get(Terran.SpiderMinePlant),
        Get(Terran.ControlTower)),
      new Pump(Terran.ControlTower)))

  class ReactiveEarlyVulture extends If(
    new And(
      new UnitsAtMost(0, Terran.MachineShop),
      new Or(
        new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway, With.fingerprints.nexusFirst),
        new EnemiesAtLeast(1, Protoss.Zealot))),
    new PumpRatio(Terran.Vulture, 1, 5, Seq(Enemy(Protoss.Zealot, 1.0))))

  class PumpScienceVessels extends PumpRatio(Terran.ScienceVessel, 1, 3, Seq(
    Enemy(Protoss.Arbiter, 1.0),
    Enemy(Protoss.DarkTemplar, 1.0)))

  class PumpGoliaths extends PumpRatio(Terran.Goliath, 0, 30, Seq(
      Enemy(Protoss.Carrier,  6.0),
      Enemy(Protoss.Arbiter,  2.0),
      Enemy(Protoss.Scout,    2.0),
      Enemy(Protoss.Shuttle,  1.0),
      Friendly(IsTank, 0.2)))
}
