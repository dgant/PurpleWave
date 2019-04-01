package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound.{If, Parallel, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpJustEnoughScourge, PumpJustEnoughZerglings, PumpMutalisks, ScoutSafelyWithOverlord}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas._
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvP3Hatch
import Strategery.Transistor

class ZvP3Hatch extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvP3Hatch)

  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new Trigger(
      new And(
        new UnitsAtLeast(9, Zerg.Drone, countEggs = true),
        new StartPositionsAtLeast(3),
        new MineralsForUnit(Zerg.Overlord, 2)),
      new Scout))

  override def aggressionPlan: Plan = new If(
    new UnitsAtLeast(40, Zerg.Hydralisk),
    new Aggression(4.0),
    new If(
      new UnitsAtLeast(30, Zerg.Hydralisk),
      new Aggression(2.2),
      new If(
        new UnitsAtLeast(15, Zerg.Hydralisk),
        new Aggression(1.5),
        new Aggression(1.15))))
  
  override def attackPlan: Plan = new Attack

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )
  
  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(Zerg.SpawningPool),
      Get(11, Zerg.Drone)),
    new FlipIf(
      new Or(
        new StartPositionsAtMost(2),
        new Check(() => With.geography.ourNatural.units.exists(u => u.isEnemy && ! u.flying))),
      new BuildOrder(Get(2, Zerg.Hatchery)),
      new If(
        new Not(new EnemyStrategy(With.fingerprints.forgeFe)),
        new BuildOrder(Get(6, Zerg.Zergling)))),
    new BuildOrder(Get(12, Zerg.Drone)))
  
  private class TakeSecondGasForMuta extends If(
    new And(
      new UnitsAtLeast(1, Zerg.Spire),
      new Or(
        new MineralsAtLeast(250),
        new UnitsAtLeast(15, Zerg.Drone, countEggs = true))),
    new BuildGasPumps(2))
  
  private class TakeThirdGasForMuta extends If(
    new And(
      new UnitsAtLeast(1, Zerg.Spire),
      new Or(
        new MineralsAtLeast(550),
        new UnitsAtLeast(21, Zerg.Drone, countEggs = true))),
    new BuildGasPumps)

  class CapGasForHydralisks extends If(
    new UnitsExactly(0, Zerg.HydraliskDen),
    new CapGasAt(50),
    new If(
      new GasForUpgrade(Zerg.HydraliskRange),
      new CapGasAt(50),
      new CapGasAt(175)))

  class Go3HatchSpeedling extends Parallel(
    new RequireMiningBases(2),
    new Pump(Zerg.Drone, 11),

    new If(
      new TwoBaseProtoss,

      // Transitions against 2-Base Protoss
      new If(
        // Transistor is really hard to bust with Hydralisks
        new OnMap(Transistor),

        // Transition to 2-Hatch Muta
        new Parallel(
          new CapGasAtRatioToMinerals(1.0, 100),
          new Pump(Zerg.Drone, 11),
          new RequireBases(2),
          new PumpJustEnoughScourge,
          new If(
            new UnitsAtLeast(1, Zerg.Spire),
            new Parallel(
              new Pump(Zerg.Drone, 18),
              new BuildOrder(Get(6, Zerg.Mutalisk)))), // Won't actually happen but ensures we save the larvae
          new BuildGasPumps(1),
          new TakeSecondGasForMuta,
          new TakeThirdGasForMuta,
          new PumpJustEnoughZerglings,
          new Pump(Zerg.Drone, 25),
          new If(
            new UnitsAtLeast(1, Zerg.Extractor, complete = true),
            new Parallel(
              new Build(
                Get(Zerg.Lair),
                Get(Zerg.ZerglingSpeed),
                Get(Zerg.Spire)))),
          new RequireBases(3),
          new Pump(Zerg.Zergling),
          new Trigger(
            new And(
              new MineralsAtLeast(800),
              new UnitsAtLeast(1, Zerg.Spire, complete = true)),
            new RequireBases(4))),

        // Transition to 3-Hatch Hydra
        new Parallel(
          new CapGasForHydralisks,
          new Pump(Zerg.Drone, 13),
          new RequireBases(3),
          new Build(
            Get(Zerg.Extractor),
            Get(Zerg.HydraliskDen)),
          new PumpJustEnoughZerglings(minimum = 2),
          new Pump(Zerg.Drone))),

      // vs. 1-base Protoss
      //
      // Transition to 3-Hatch Muta
      new Parallel(
        new If(
          new UnitsAtMost(0, Zerg.Lair),
          new If(
            new Or(
              new MineralsAtLeast(300),
              new EnemyHasShown(Protoss.Stargate),
              new EnemyHasShown(Protoss.Corsair),
              new EnemyHasShown(Protoss.Scout),
              new Not(new GasForUpgrade(Zerg.ZerglingSpeed)),
              new BasesAtLeast(3)),
            new CapGasAt(100),
            new CapGasAt(0)),
          new If(
            new UnitsAtMost(0, Zerg.Spire),
            new CapGasAt(150),
            new CapGasAtRatioToMinerals(1.0, 100))),
        new Pump(Zerg.Drone, 11),
        new BuildOrder(
          Get(12, Zerg.Zergling),
          Get(13, Zerg.Drone)),

        // Avoid investing in these until we know it's a one-base Protoss
        new If(
          new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway),
          new Parallel(
            new BuildSunkensAtNatural(2),
            new Pump(Zerg.SunkenColony),
            new Build(
              Get(Zerg.Extractor),
              Get(Zerg.ZerglingSpeed)))),
        new TechToSpireVsAir,
        new BuildOrder(Get(12, Zerg.Zergling)),
        new PumpRatio(Zerg.Zergling, 4, 30, Seq(
          Enemy(Protoss.Zealot, 4.0),
          Enemy(Protoss.Dragoon, 4.0),
          Friendly(UnitMatchAnd(Zerg.SunkenColony, UnitMatchComplete), -4.0))),
        new RequireBases(3),
        new Pump(Zerg.Drone, 12),
        new Build(
          Get(Zerg.Extractor),
          Get(Zerg.Lair),
          Get(Zerg.Spire)),
        new Pump(Zerg.Drone, 18),
        new Pump(Zerg.Zergling),
        new TakeSecondGasForMuta,
        new TakeThirdGasForMuta)))

  class Go3HatchHydra extends Parallel(
    new CapGasForHydralisks,
    new Pump(Zerg.Drone, 12),
    new Build(Get(Zerg.SpawningPool), Get(Zerg.Extractor), Get(Zerg.HydraliskDen)),
    new UpgradeContinuously(Zerg.HydraliskSpeed),
    new PumpRatio(Zerg.Hydralisk, 0, 4, Seq(Enemy(Protoss.Zealot, 1.0))),
    new If(
      new UpgradeComplete(Zerg.HydraliskSpeed),
      new UpgradeContinuously(Zerg.HydraliskRange)),
    new PumpRatio(Zerg.Drone, 14, 30, Seq(Friendly(Zerg.HatcheryLairOrHive, 6.0))),
    new PumpRatio(Zerg.Extractor, 1, 4, Seq(Friendly(Zerg.Drone, 1.0 / 9.0))),
    new Pump(Zerg.Hydralisk),
    new PumpRatio(Zerg.Hatchery, 3, 6, Seq(Friendly(Zerg.Drone, 1.0/6.0))),
    new If(new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true)),
    new Pump(Zerg.Drone))

  class Go23HatchMuta extends Parallel(
    new CapGasAtRatioToMinerals(1.0, 100),
    new PumpJustEnoughScourge,
    new Pump(Zerg.Drone, 12),
    new BuildOrder(Get(6, Zerg.Mutalisk)),
    new If(
      new UnitsAtLeast(6, Zerg.Mutalisk, complete = true),
      new Pump(Zerg.Drone, 21)),
    new Pump(Zerg.Drone, 16),
    new Build(Get(Zerg.SpawningPool), Get(Zerg.Extractor)),
    new Build(Get(Zerg.Lair)),
    new Build(Get(Zerg.Spire), Get(2, Zerg.Extractor)),
    new PumpJustEnoughScourge,
    new PumpMutalisks,
    new Pump(Zerg.Zergling),
    new RequireMiningBases(3))

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new UnitsAtLeast(3, Zerg.Hatchery, complete = true),
      new EjectScout),
    new OverlordSpeedVsCloakedThreats,
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool),
      new Parallel(
        new If(
          new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true),
          new Go3HatchHydra,
          new If(
            new UnitsAtLeast(1, Zerg.Spire, complete = true),
            new Go23HatchMuta,
            new Go3HatchSpeedling)))))
}

