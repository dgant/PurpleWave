package Planning.Plans.GamePlans.Zerg.ZvPNew

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, EjectScout}
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtMost, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.{ZvPBust, ZvPStandard}

class ZvPMain extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvPStandard, ZvPBust)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Zerg.Hatchery))

  override def scoutPlan: Plan = new ScoutSafelyWithOverlord
  override def attackPlan: Plan = new ZvPIdeas.AttackPlans

  class AttemptMutaliskBust extends And(
    new UnitsAtLeast(1, Zerg.Spire),
    new EnemiesAtMost(0, Protoss.Corsair),
    new EnemiesAtMost(8, Protoss.Dragoon))

  override def buildPlans: Seq[Plan] = Seq(

    new EjectScout,
    new CapGasAtRatioToMinerals(1.0, 300),

    new Pump(Zerg.Drone, 12),
    new Build(Get(Zerg.SpawningPool)),
    new RequireMiningBases(3),
    new Build(Get(Zerg.Extractor)),

    new If(
      new And(
        new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe),
        new Latch(new EnemiesAtMost(0, Protoss.PhotonCannon)),
        new Or(
          new Employing(ZvPBust),
          new Latch(new EnemiesAtMost(2, Protoss.PhotonCannon)),
        new EnemiesAtMost(4, Protoss.PhotonCannon),
        new UnitsAtMost(4, Zerg.Hatchery))),

      // Hydralisk Bust!
      new Parallel(
        new Pump(Zerg.Drone, 20),
        new Aggression(1.2),
        new Build(
          Get(Zerg.HydraliskDen),
          Get(Zerg.HydraliskSpeed),
          Get(Zerg.HydraliskRange)),
        new If(
          new UnitsAtLeast(14, Zerg.Drone),
          new Build(Get(2, Zerg.Extractor))),
        new BuildOrder(Get(24, Zerg.Hydralisk)),
        new Pump(Zerg.Drone),
        new Build(Get(5, Zerg.Hatchery))),

      // Don't Hydralisk bust!
      new Parallel(
        // Mutalisk bust!
        new If(
          new AttemptMutaliskBust,
          new Aggression(1.2),
          new Parallel(
            new PumpRatio(Zerg.Extractor, 1, 3, Seq(Friendly(Zerg.Drone, 1/9.0))),
            new BuildOrder(
              Get(6, Zerg.Overlord),
              Get(8, Zerg.Mutalisk)),
            new Pump(Zerg.Mutalisk))),

        new If(
          new Or(
            new Not(new AttemptMutaliskBust),
            new UnitsAtLeast(1, Zerg.Spire, complete = true)),
          new Parallel(

            // Get just enough army to defend while we go five Hatch
            new If(
              new And(
                new UnitsAtLeast(30, UnitMatchWarriors, countEggs = true),
                new SafeAtHome),
              new Pump(Zerg.Drone, 30)),

            new Build(
              Get(Zerg.Lair),
              Get(Zerg.ZerglingSpeed),
              Get(Zerg.Spire)),

            new PumpRatio(Zerg.Hydralisk, 12, 24, Seq(Enemy(UnitMatchWarriors, 1.5), Friendly(Zerg.Zergling, -0.25), Friendly(Zerg.Mutalisk, -2.0))),
            new PumpRatio(Zerg.Zergling, 12, 24, Seq(Enemy(UnitMatchWarriors, 4.0), Friendly(Zerg.Hydralisk, -4.0), Friendly(Zerg.Mutalisk, -7.0))),
            new PumpRatio(Zerg.Scourge, 0, 8, Seq(Flat(2), Enemy(Protoss.Corsair, 2))),

            new PumpWorkers,
            new Trigger(
              new UnitsAtLeast(1, Zerg.Spire),
              new Build(
                Get(Zerg.HydraliskDen),
                Get(Zerg.HydraliskSpeed),
                Get(Zerg.HydraliskRange),
                Get(5, Zerg.Hatchery),
                Get(Zerg.OverlordSpeed)))
  )))))

}
