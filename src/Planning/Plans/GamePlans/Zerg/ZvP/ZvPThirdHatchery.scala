package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Predicates.Always
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones.{EnemiesAtMost, GasForUpgrade, UnitsAtLeast}
import Planning.Predicates.Strategy.EnemyStrategy
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}

class ZvPThirdHatchery extends GameplanTemplate {

  override val activationCriteria: Predicate = new Always
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(3, Zerg.Hatchery))

  override def buildPlans: Seq[Plan] = Seq(

    new If(
      new EnemyStrategy(With.fingerprints.nexusFirst),
      new Parallel(
        // Try to kill them
        new Pump(Zerg.Drone, 9),
        new BuildOrder(
          Get(Zerg.SpawningPool),
          Get(11, Zerg.Drone)),
        new Pump(Zerg.Zergling),
        new BuildOrder(
          Get(Zerg.Extractor),
          Get(Zerg.ZerglingSpeed)),
        new If(new GasForUpgrade(Zerg.ZerglingSpeed), new CapGasWorkersAt(0)),
        new Pump(Zerg.Zergling),
        new RequireMiningBases(3)),

      new If(
        new EnemyStrategy(With.fingerprints.forgeFe),
        new Parallel(
          new Pump(Zerg.Drone, 13),
          new RequireMiningBases(3),
          new Pump(Zerg.Zergling, 2),
          new Pump(Zerg.Drone)),

        new If(
          new EnemyStrategy(With.fingerprints.gatewayFe),
          new Parallel(
            new BuildOrder(
              Get(Zerg.SpawningPool),
              Get(8, Zerg.Zergling)),
            new PumpRatio(Zerg.Zergling, 0, 12, Seq(Enemy(Protoss.Zealot, 4.0))),
            new Pump(Zerg.Drone, 13),
            new RequireMiningBases(3),
            new Pump(Zerg.Drone)),

          new If(
            new EnemyStrategy(With.fingerprints.cannonRush),
            new Parallel(
              new If(
                new EnemiesAtMost(2, Protoss.PhotonCannon, complete = true),
                new Pump(Zerg.Zergling)),
              new Pump(Zerg.Drone, 12),
              new BuildSunkensAtNatural(2),
              new BuildOrder(
                Get(Zerg.Extractor),
                Get(Zerg.HydraliskDen)),
              new Pump(Zerg.Drone, 16),
              new BuildOrder(Get(6, Zerg.Hydralisk)),
              new RequireMiningBases(3),
              new Pump(Zerg.Hydralisk)),

            // Otherwise, act as if it's a two-gate
            new Parallel(
              new Build(Get(Zerg.SpawningPool)),
              new BuildOrder(Get(14, Zerg.Drone)),
              new BuildSunkensAtNatural(2),
              new PumpRatio(Zerg.Zergling, 8, 24, Seq(Enemy(Protoss.Zealot, 4.5))),
              new RequireMiningBases(3))))))

  )
}
