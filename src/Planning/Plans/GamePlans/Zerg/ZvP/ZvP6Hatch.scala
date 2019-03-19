package Planning.Plans.GamePlans.Zerg.ZvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Basic.Do
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{ScoutSafelyWithOverlord, UpgradeHydraSpeedThenRange}
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas.OverlordSpeedVsDarkTemplar
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelIncomplete
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.MineralsAtMost
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete}
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
        new Not(new EnemyStrategy(With.fingerprints.twoGate)),
        new Scout)))

  override def attackPlan: Plan = new Attack

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
          initialBefore = new CancelIncomplete(Zerg.Extractor, Zerg.CreepColony, Zerg.SunkenColony)),
        new BuildOrder(
          Get(14, Zerg.Drone),
          Get(3, Zerg.Hatchery),
          Get(19, Zerg.Drone),
          Get(3, Zerg.Overlord),
          Get(21, Zerg.Drone),
          Get(4, Zerg.Hatchery),
          Get(25, Zerg.Drone),
          Get(5, Zerg.Hatchery),
          Get(Zerg.SpawningPool),
          Get(Zerg.Extractor),
          Get(29, Zerg.Drone),
          Get(Zerg.HydraliskDen),
          Get(4, Zerg.Overlord)))))

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen),
      new CapGasAt(150),
      new If(
        new MineralsAtMost(1000),
        new CapGasAtRatioToMinerals(1.0))),

    new If(
      new Not(new EnemyStrategy(With.fingerprints.forgeFe)),

      new If(
        new EnemyStrategy(With.fingerprints.gatewayFe),

        // Gateway FE reaction
        new Parallel(
          new Build(Get(Zerg.SpawningPool)),
          new If(
            new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
            new PumpRatio(Zerg.Zergling, 8, 24, Seq(Enemy(Protoss.Zealot, 4.0)))),
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
          new If(
            new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
            new PumpRatio(Zerg.Zergling, 8, 24, Seq(Enemy(Protoss.Zealot, 4.0), Friendly(UnitMatchAnd(UnitMatchComplete, Zerg.SunkenColony), -6.0)))))
      )),

    // Make sure we get the basics
    new Pump(Zerg.Drone, 12),
    new Build(Get(Zerg.SpawningPool)),
    new Pump(Zerg.Drone, 16),
    new Build(Get(Zerg.Extractor), Get(Zerg.HydraliskDen)),
    new Pump(Zerg.Drone, 24),
    new UpgradeHydraSpeedThenRange,
    new OverlordSpeedVsDarkTemplar,
    new If(
      new And(
        new UnitsAtLeast(18, Zerg.Hydralisk),
        new UnitsAtLeast(5, Zerg.Hatchery),
        new UpgradeStarted(Zerg.HydraliskRange)),
      new Parallel(
        new Build(Get(Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundRangeDamage),
        new Build(Get(Zerg.Lair)))),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        Get(Zerg.Lair),
        Get(Zerg.OverlordSpeed))),
    new PumpRatio(Zerg.Hydralisk, 1, 200, Seq(Enemy(Protoss.Corsair, 2.0), Enemy(Protoss.Scout, 3.0))),
    new PumpRatio(Zerg.Hydralisk, 0, 48, Seq(Enemy(Protoss.Dragoon, 3.0), Enemy(Protoss.Zealot, 2.0), Enemy(Protoss.Archon, 6.0), Friendly(Zerg.Zergling, -0.25))),
    new RequireMiningBases(5),
    new PumpRatio(Zerg.Extractor, 2, 12, Seq(Friendly(Zerg.Drone, 1.0 / 14.0))),
    new Pump(Zerg.Drone, 34),
    new Pump(Zerg.Hydralisk, 24),
    new Pump(Zerg.Drone, 50),
    new If(
      new UpgradeStarted(Zerg.HydraliskRange),
      new PumpRatio(Zerg.Hatchery, 3, 9, Seq(Friendly(Zerg.Drone, 6.0))))
  )
}
