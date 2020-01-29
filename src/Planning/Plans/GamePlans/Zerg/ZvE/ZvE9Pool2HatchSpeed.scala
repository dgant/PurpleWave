package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Aggression, AllIn, Attack}
import Planning.Plans.Compound.{If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.GamePlans.Zerg.ZvZ.ZvZIdeas
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutOn}
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{EnemyIsZerg, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchHatchery
import ProxyBwapi.Races.Zerg

class ZvE9Pool2HatchSpeed extends GameplanTemplate {

  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new If(
      new Not(new FoundEnemyBase),
      new ScoutOn(Zerg.Overlord, quantity = 2)))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  override def aggressionPlan: Plan = new If(
    new UpgradeComplete(Zerg.ZerglingSpeed),
    new Aggression(1.5))

  override def attackPlan: Plan = new If(
    new EnemyIsZerg,
    super.attackPlan,
    new Attack)

  override val buildOrder = Seq(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    // Extractor trick?
    Get(2, Zerg.Overlord),
    Get(6, Zerg.Zergling))

  override def buildPlans: Seq[Plan] = Seq(
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
          new AllIn),

        new Build(Get(2, Zerg.Hatchery)),
        new If(
          new Or(
            new UnitsAtLeast(2, UnitMatchHatchery),
            new MineralsAtLeast(350)),
          new If(
            new GasForUpgrade(Zerg.ZerglingSpeed),
            new CapGasWorkersAt(0),
            new Build(Get(Zerg.Extractor)))),
        new If(new GasAtLeast(100), new Build(Get(Zerg.ZerglingSpeed))),
        new Pump(Zerg.Zergling)))
  )
}
