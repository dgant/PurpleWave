package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, Chill, ConsiderAttacking, EjectScout}
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Predicates.Compound.{And, Latch, Not, Sticky}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeToMoveOut
import Planning.Predicates.Strategy.EnemyStrategy
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}

class ZvTMidgame extends GameplanTemplate {

  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(1, Zerg.Hive))

  class GoMutalisk extends Sticky(new Not(new EnemyStrategy(With.fingerprints.fiveRax, With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.oneRaxFE)))

  // Avoid suiciding Hydralisks destined to become Lurkers
  override def priorityDefensePlan: Plan = new If(
    new And(
      new Not(new SafeToMoveOut),
      new TechStarted(Zerg.LurkerMorph)),
    new Chill(Zerg.Hydralisk))

  override def attackPlan: Plan = new Parallel(
    new Attack(Zerg.Mutalisk),
    new If(
      new Or(
        new UnitsAtLeast(1, Zerg.Lurker, complete = true),
        new EnemiesAtMost(0, Terran.Vulture),
        new And(
          new UpgradeComplete(Zerg.ZerglingSpeed),
          new Not(new EnemyHasUpgrade(Terran.VultureSpeed)))),
      new ConsiderAttacking))

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new Build(Get(12, Zerg.Drone)),
    new If(new And(new UnitsAtLeast(1, Zerg.Lair), new Not(new GoMutalisk)), new Build(Get(Zerg.HydraliskDen))),
    new If(new And(new UnitsAtLeast(1, Zerg.Lair), new UnitsAtLeast(1, Zerg.HydraliskDen)), new Build(Get(Zerg.LurkerMorph))),
    new RequireMiningBases(2),
    new Build(
      Get(Zerg.SpawningPool),
      Get(Zerg.Extractor),
      Get(Zerg.ZerglingSpeed),
      Get(Zerg.Lair)),
    new If(new EnemyStrategy(With.fingerprints.oneRaxGas), new BuildSunkensAtNatural(1)),
    new If(new Or(new EnemyStrategy(With.fingerprints.oneRaxGas), new EnemiesAtLeast(1, Terran.Vulture)), new Build(Get(Zerg.Burrow))),
    new If(new EnemyHasShownWraithCloak, new UpgradeContinuously(Zerg.OverlordSpeed)),

    new FlipIf(
      new And(
        new UnitsAtLeast(1, Zerg.EvolutionChamber),
        new UnitsAtLeast(30, Zerg.Drone)),
      new Parallel(
        new Pump(Zerg.SunkenColony),
        new Pump(Zerg.Mutalisk, 12),
        new Pump(Zerg.Lurker),
        new PumpRatio(Zerg.Hydralisk, 0, 12, Seq(Flat(8), Friendly(Zerg.Lurker, -1))),
        new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(Terran.Marine, 3), Enemy(Terran.Vulture, 6))),
        new If(
          new GoMutalisk,
          new Parallel(
            new WriteStatus("Mutalisks"),
            new Build(Get(Zerg.Spire)),
            new If(new UnitsAtLeast(1, Zerg.Spire), new Build(Get(Zerg.Extractor))),
            new UpgradeContinuously(Zerg.AirArmor)),
          new Parallel(
            new WriteStatus("Lurkers"),
            new Build(
              Get(Zerg.HydraliskDen),
              Get(2, Zerg.Extractor)))),
        new PumpWorkers,
        new RequireMiningBases(3)),
      new Parallel(
        new Build(
          Get(3, Zerg.Extractor),
          Get(Zerg.EvolutionChamber),
          Get(Zerg.OverlordSpeed),
          Get(Zerg.QueensNest),
          Get(Zerg.Burrow),
          Get(Zerg.Hive)),
        new UpgradeContinuously(Zerg.GroundArmor)))
  )
}
