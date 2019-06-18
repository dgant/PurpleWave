package Planning.Plans.GamePlans.Zerg.ZvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, EjectScout, Hunt}
import Planning.Plans.Basic.Do
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpJustEnoughScourge, ScoutSafelyWithOverlord, UpgradeHydraSpeedThenRange}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas.{BurrowVsReaver, OverlordSpeedVsCloakedThreats}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.BuildSunkensAtNatural
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.MineralsAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtMost
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers._
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvP6Hatch

class ZvP6Hatch extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvP6Hatch)

  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new Trigger(
      new UnitsAtLeast(2, Zerg.Overlord, countEggs = true),
      new If(
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.forgeFe)),
        new Scout)))

  override def attackPlan: Plan = new Parallel(
    new Hunt(Zerg.Scourge, Protoss.Shuttle),
    new Hunt(Zerg.Scourge, Protoss.Corsair),
    new Attack
  )

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override def buildOrderPlan = new Parallel(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = GameTime(1, 0)()),
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(12, Zerg.Drone),
      Get(2, Zerg.Hatchery)),
    new If(
      new EnemyStrategy(With.fingerprints.forgeFe),
      new Parallel(
        new Trigger(
          new UnitsAtLeast(5, Zerg.Hatchery),
          initialBefore = new CancelIncomplete(Zerg.CreepColony, Zerg.SunkenColony)),
        new BuildOrder(
          Get(14, Zerg.Drone),
          Get(3, Zerg.Hatchery),
          Get(19, Zerg.Drone),
          Get(3, Zerg.Overlord),
          Get(20, Zerg.Drone),
          Get(4, Zerg.Hatchery),
          Get(29, Zerg.Drone),
          Get(4, Zerg.Overlord),
          Get(Zerg.SpawningPool),
          Get(31, Zerg.Drone),
          Get(Zerg.Extractor),
          Get(5, Zerg.Hatchery),
          Get(2, Zerg.Extractor),
          Get(Zerg.HydraliskDen),
          Get(6, Zerg.Zergling),
          Get(Zerg.HydraliskSpeed),
          Get(Zerg.Lair)))))

  class GetSufficientZerglings extends PumpRatio(Zerg.Zergling, 8, 24, Seq(
    Enemy(UnitMatchAnd(UnitMatchWarriors, UnitMatchNot(UnitMatchMobileFlying)), 4.0),
    Friendly(UnitMatchAnd(UnitMatchComplete, Zerg.SunkenColony), -6.0)))

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.forgeFe),
        new UnitsAtMost(0, Zerg.Lair),
        new UnitsAtMost(0, Zerg.HydraliskDen),
        new UnitsAtMost(4, Zerg.HydraliskDen)),
      new CapGasAt(50),
      new If(
        new UnitsAtMost(0, Zerg.HydraliskDen),
        new CapGasAt(150),
        new If(
          new MineralsAtMost(1000),
          new CapGasAtRatioToMinerals(1.0)))),

    new If(
      new Not(new EnemyStrategy(With.fingerprints.forgeFe)),

      new If(
        new EnemyStrategy(With.fingerprints.gatewayFe),

        // Gateway FE reaction
        new Parallel(
          new Build(Get(Zerg.SpawningPool)),
          new If(new UnitsAtMost(0, Zerg.HydraliskDen, complete = true), new GetSufficientZerglings),
          new Build(
            Get(13, Zerg.Drone),
            Get(3, Zerg.Hatchery),
            Get(16, Zerg.Drone),
            Get(5, Zerg.Hatchery),
            Get(Zerg.Extractor),
            Get(Zerg.HydraliskDen))),

        // One-base reaction
        new Parallel(
          new BuildOrder(
            Get(Zerg.SpawningPool),
            Get(13, Zerg.Drone),
            Get(8, Zerg.Zergling)),
          new BuildSunkensAtNatural(3),
          new Pump(Zerg.SunkenColony),
          new Pump(Zerg.Drone, 9),
          new FlipIf(
            new UnitsAtLeast(8, Zerg.Zergling, complete = true),
            new If(new UnitsAtMost(0, Zerg.HydraliskDen, complete = true), new GetSufficientZerglings),
            new PumpRatio(Zerg.Drone, 7, 16, Seq(Friendly(Zerg.Zergling, 0.5), Friendly(Zerg.Hydralisk, 1.0)))))
      )),

    // Make sure we get the basics
    new Pump(Zerg.Drone, 12),
    new Build(Get(Zerg.SpawningPool)),
    new Pump(Zerg.Drone, 16),
    new Build(Get(Zerg.Extractor), Get(Zerg.HydraliskDen)),
    new Pump(Zerg.Hydralisk, 2),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.gatewayFirst),
        new EnemyBasesAtMost(1)),
      new Parallel(
        new UpgradeHydraSpeedThenRange,
        new Pump(Zerg.Hydralisk, 12))),
    new Pump(Zerg.Drone, 24),
    new UpgradeHydraSpeedThenRange,
    new OverlordSpeedVsCloakedThreats,
    new BurrowVsReaver,
    new PumpJustEnoughScourge,
    new If(new Or(new EnemyHasShown(Protoss.Shuttle), new EnemyHasShown(Protoss.Corsair)), new Build(Get(Zerg.Spire))),
    new If(
      new And(
        new UnitsAtLeast(18, Zerg.Hydralisk),
        new UnitsAtLeast(5, Zerg.Hatchery),
        new UpgradeStarted(Zerg.HydraliskRange)),
      new Parallel(
        new Build(Get(Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundRangeDamage),
        new Build(Get(Zerg.Lair)))),
    new PumpRatio(Zerg.Hydralisk, 1, 200, Seq(Enemy(Protoss.Corsair, 2.0), Enemy(Protoss.Scout, 3.0))),
    new PumpRatio(Zerg.Hydralisk, 0, 48, Seq(Enemy(Protoss.Dragoon, 3.0), Enemy(Protoss.Zealot, 2.0), Enemy(Protoss.Archon, 6.0), Enemy(Protoss.Reaver, 5.0), Friendly(Zerg.Zergling, -0.25))),
    new RequireMiningBases(5),
    new PumpRatio(Zerg.Extractor, 2, 12, Seq(Friendly(Zerg.Drone, 1.0 / 14.0), Friendly(Zerg.Spire, 1.0))),
    new Pump(Zerg.Drone, 30),
    new If(
      new And(
        new TechStarted(Zerg.LurkerMorph),
        new UnitsAtLeast(34, Zerg.Drone)),
      new BuildGasPumps),
    new PumpRatio(Zerg.Lurker, 2, 24, Seq(Enemy(Protoss.Zealot, 0.5), Enemy(Protoss.Reaver, -2.0), Enemy(Protoss.Dragoon, -0.5))),
    new Pump(Zerg.Hydralisk, 24),
    new Build(Get(Zerg.Lair)),
    new If(new EnemiesAtLeast(5, Protoss.Zealot), new Build(Get(Zerg.LurkerMorph))),
    new Pump(Zerg.Drone, 50),
    new If(new UpgradeStarted(Zerg.HydraliskRange), new PumpRatio(Zerg.Hatchery, 3, 9, Seq(Friendly(Zerg.Drone, 6.0)))),
  )
}
