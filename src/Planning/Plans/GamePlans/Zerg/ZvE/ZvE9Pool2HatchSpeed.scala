package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, AllInIf, AttackAndHarass}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvZ.ZvZIdeas
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{EnemyIsZerg, EnemyStrategy}
import Utilities.UnitFilters.IsHatchlike
import ProxyBwapi.Races.Zerg

class ZvE9Pool2HatchSpeed extends GameplanTemplate {

  override def scoutPlan: Plan = NoPlan()

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  override def attackPlan: Plan = new If(
    new EnemyIsZerg,
    super.attackPlan,
    new AttackAndHarass)

  override val buildOrder = Seq(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    // Extractor trick?
    Get(2, Zerg.Overlord),
    Get(6, Zerg.Zergling))

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new Aggression(1.5)),
    new Pump(Zerg.Drone, 6),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new Parallel(
        new Pump(Zerg.Mutalisk),
        new Pump(Zerg.Zergling),
        new Build(
          Get(9, Zerg.Drone),
          Get(Zerg.Extractor),
          Get(Zerg.Lair),
          Get(Zerg.Spire))),

      new Parallel(
        // We have no economic advantage over anyone except a 4-pooler, so for us Zergling speed is our one timing to attack
        new If(
          new Or(
            new EnemiesAtLeast(1, Zerg.Spire, complete = true),
            new EnemyHasShown(Zerg.Mutalisk),
            new And(
              new UpgradeComplete(Zerg.ZerglingSpeed),
              new Not(new EnemyHasUpgrade(Zerg.ZerglingSpeed)),
              new Or(
                new EnemyBasesAtLeast(2),
                new EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.twelveHatch, With.fingerprints.twelvePool)))),
          new AllInIf),

        new Build(Get(2, Zerg.Hatchery)),
        new If(new UnitsAtLeast(2, Zerg.Hatchery), new Build(Get(Zerg.Extractor))),
        new If(
          new Or(
            new UnitsAtLeast(2, IsHatchlike),
            new MineralsAtLeast(350)),
          new If(
            new GasForUpgrade(Zerg.ZerglingSpeed),
            new CapGasWorkersAt(0),
            new Build(Get(Zerg.Extractor)))),
        new If(new GasAtLeast(100), new Build(Get(Zerg.ZerglingSpeed))),
        new Pump(Zerg.Zergling)))
  )
}
