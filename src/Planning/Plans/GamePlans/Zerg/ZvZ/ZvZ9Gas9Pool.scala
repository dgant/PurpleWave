package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, ConsiderAttacking}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Zerg.BuildSunkensInMain
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ9Gas9Pool

class ZvZ9Gas9Pool extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ9Gas9Pool)

  override def scoutPlan: Plan = new ScoutSafelyWithOverlord

  override def attackPlan: Plan = new Parallel(
    new Attack(Zerg.Mutalisk),
    new Trigger(
      new UpgradeComplete(Zerg.ZerglingSpeed, 1, 32 * 4),
      new ConsiderAttacking))

  override def aggressionPlan: Plan = new If(
    new And(
      new EnemiesAtMost(0, Zerg.Mutalisk),
      new UpgradeComplete(Zerg.ZerglingSpeed, 1, 24),
      new Not(new EnemyHasUpgrade(Zerg.ZerglingSpeed))),
    new Aggression(2.0),
    new Aggression(1.0))

  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(Get(9, Zerg.Drone)),
    new Trigger(
      new MineralsAtLeast(100),
      new BuildOrder(
        Get(Zerg.Extractor),
        Get(10, Zerg.Drone))),
    new Trigger(
      new MineralsAtLeast(250),
      new Parallel(
        new BuildOrder(
          Get(Zerg.SpawningPool),
          Get(11, Zerg.Drone),
          Get(2, Zerg.Overlord),
          Get(13, Zerg.Drone),
          Get(Zerg.ZerglingSpeed),
          Get(Zerg.Lair),
          Get(6, Zerg.Zergling)))))

  override def supplyPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Zerg.SpawningPool),
    super.supplyPlan)

  override def buildPlans: Seq[Plan] = Vector(
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool),
      new Parallel(
        new If(
          new UnitsAtLeast(1, Zerg.Spire),
          new CapGasAtRatioToMinerals(1.0, 75),
          new If(
            new And(
              new UnitsAtLeast(1, Zerg.Lair),
              new UnitsAtMost(0, Zerg.Spire)),
            new CapGasWorkersAt(2),
            new CapGasWorkersAt(3))),

        new Pump(Zerg.SunkenColony),
        new If(
          new EnemyStrategy(With.fingerprints.fourPool, With.fingerprints.tenHatch, With.fingerprints.twelveHatch),
          new BuildSunkensInMain(1)),

        new Pump(Zerg.Drone, 8),

        new Build(Get(Zerg.Spire)),
        new If(
          new UnitsAtLeast(1, Zerg.Spire),
          new BuildOrder(Get(3, Zerg.Mutalisk))),
        new Pump(Zerg.Mutalisk),
        new Pump(Zerg.Zergling),
        new BuildSunkensInMain(1),
        new If(
          new GasAtLeast(200),
          new Build(
            Get(Zerg.AirArmor),
            Get(Zerg.AirDamage)))
  )))
}
