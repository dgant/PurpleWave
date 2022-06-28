package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army.{Aggression, AttackAndHarass}
import Planning.Plans.Basic.{NoPlan, WriteStatus}
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.BuildSunkensAtNatural
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Utilities.UnitFilters.{IsAll, IsComplete, IsAny, IsWarrior}
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.{ZvPHydraBust, ZvPMutaliskBust, ZvPReactiveBust, ZvPZerglingBust}

class ZvPMain extends GameplanTemplate {

  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Zerg.Hatchery))

  override def attackPlan: Plan = new AttackAndHarass
  override def scoutPlan: Plan = NoPlan()

  class AttemptZerglingBust extends And(
    new UnitsAtMost(0, Zerg.Lair),
    new UnitsAtMost(0, Zerg.Spire),
    new Or(
      new Employing(ZvPZerglingBust),
      new And(
        new Employing(ZvPReactiveBust),
        new EnemyStrategy(With.fingerprints.proxyGateway, With.fingerprints.oneGateCore))))

  class AttemptHydraliskBust extends And(
    new UnitsAtMost(0, Zerg.Lair),
    new UnitsAtMost(0, Zerg.Spire),
    new Or(
      new Employing(ZvPHydraBust),
      new And(
        new Employing(ZvPReactiveBust),
        new EnemyStrategy(With.fingerprints.forgeFe),
        new EnemiesAtMost(1, Protoss.PhotonCannon, complete = true))))

  class AttemptMutaliskBust extends Or(
    new Employing(ZvPMutaliskBust),
    new And(
      new Employing(ZvPReactiveBust),
      new EnemiesAtMost(0, Protoss.Corsair),
      new EnemiesAtMost(0, Protoss.Archon),
      new EnemiesAtMost(12, Protoss.Dragoon)))

  class DoZerglingBust extends Parallel(
    new WriteStatus("Zergling Bust"),

    new BuildOrder(
      Get(Zerg.Extractor),
      Get(Zerg.ZerglingSpeed)),
    new PumpRatio(Zerg.Drone, 12, 18, Seq(Friendly(IsAll(IsComplete, Zerg.Hatchery), 4.0))),
    new If(
      new EnemiesAtLeast(1, IsAny(Protoss.Stargate, Protoss.Corsair, Protoss.Scout)),
      new Parallel(
        new Build(Get(Zerg.Lair), Get(Zerg.Spire)),
        new ZvPIdeas.PumpScourgeAgainstAir),
      new If(
        new GasForUpgrade(Zerg.ZerglingSpeed),
        new CapGasAt(0))),
    new If(
      new EnemyHasShown(Protoss.DarkTemplar),
      new UpgradeContinuously(Zerg.OverlordSpeed)),
    new Pump(Zerg.Zergling),
    new Build(Get(5, Zerg.Hatchery)))

  class DoHydraliskBust extends Parallel(
    new WriteStatus("Hydralisk Bust"),
    new Aggression(1.2),
    new CapGasWorkersAt(4),
    new CapGasAt(225),
    new PumpRatio(Zerg.Drone, 21, 35, Seq(Friendly(IsAll(IsComplete, Zerg.Hatchery), 7.0))),
    new Build(Get(Zerg.HydraliskDen), Get(Zerg.HydraliskSpeed)),
    new If(new UpgradeComplete(Zerg.HydraliskSpeed),new Build(Get(Zerg.HydraliskRange))),
    new If(new UnitsAtLeast(14, Zerg.Drone), new Build(Get(2, Zerg.Extractor))),
    new Trigger(new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true), new BuildOrder(Get(30, Zerg.Hydralisk), Get(5, Zerg.Hatchery))),
    new Pump(Zerg.Hydralisk))

  class DoMutaliskBust extends Parallel(
    new WriteStatus("Mutalisk Bust"),
    new Aggression(1.2),
    new Parallel(
    new PumpRatio(Zerg.Extractor, 1, 3, Seq(Flat(-1), Friendly(Zerg.Drone, 1/9.0))),
    new BuildOrder(
      Get(Zerg.Extractor),
      Get(Zerg.Lair),
      Get(Zerg.Spire),
      Get(Zerg.ZerglingSpeed)),

    new If(
      new Or(
        new EnemiesAtLeast(1, Protoss.Forge),
        new EnemyBasesAtLeast(2)),
      new BuildOrder(
        Get(29, Zerg.Drone),
        Get(6, Zerg.Overlord)),
      new BuildOrder(
        Get(23, Zerg.Drone),
        Get(5, Zerg.Overlord))),

    new Pump(Zerg.Mutalisk),
    new Pump(Zerg.Zergling, 12),
    new BuildSunkensAtNatural(1),
    new Trigger(
      new UnitsAtLeast(6, Zerg.Mutalisk, complete = true),
      new Build(Get(5, Zerg.Hatchery))),
    new PumpWorkers,
    new Pump(Zerg.Zergling)))

  class DoFiveHatch extends Parallel(
    new WriteStatus("5-Hatch"),

    // Get just enough army to defend while we go five Hatch
    new If(
      new And(
        new UnitsAtLeast(30, IsWarrior),
        new SafeAtHome),
      new Pump(Zerg.Drone, 30)),

    new Build(Get(Zerg.Lair), Get(Zerg.ZerglingSpeed)),

    new If(
      new Or(
        new Not(new EnemyHasUpgrade(Protoss.DragoonRange)),
        new EnemyHasShown(Protoss.Corsair)),
      new Build(Get(Zerg.Spire))),

    new PumpRatio(Zerg.Hydralisk, 12, 24, Seq(Enemy(IsWarrior, 1.5), Friendly(Zerg.Zergling, -0.25), Friendly(Zerg.Mutalisk, -2.0))),
    new ZvPIdeas.PumpScourgeAgainstAir,
    new PumpRatio(Zerg.Zergling, 12, 24, Seq(Enemy(IsWarrior, 4.0), Friendly(Zerg.Hydralisk, -4.0), Friendly(Zerg.Mutalisk, -7.0))),

    new PumpWorkers,
    new Build(
      Get(Zerg.HydraliskDen),
      Get(Zerg.HydraliskSpeed),
      Get(Zerg.HydraliskRange),
      Get(5, Zerg.Hatchery))
  )

  override def buildPlans: Seq[Plan] = Seq(
    new CapGasAtRatioToMinerals(1.0, 300),
    new Pump(Zerg.Drone, 12),
    new RequireMiningBases(2),
    new BuildOrder(
      Get(Zerg.Extractor),
      Get(Zerg.SpawningPool),
      Get(2, Zerg.Zergling)),
    new Pump(Zerg.SunkenColony),
    new RequireMiningBases(3),

    new If(
      new AttemptZerglingBust,
      new DoZerglingBust,
      new If(
        new AttemptHydraliskBust,
        new DoHydraliskBust,
        new If(
          new AttemptMutaliskBust,
          new DoMutaliskBust,
          new DoFiveHatch)))
  )
}
