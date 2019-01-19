package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas._
import Planning.Plans.GamePlans.Zerg.ZvP.ZvPIdeas.{OverpoolBuildLarvaOrDrones, OverpoolSpendLarva, TwoBaseProtoss}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Scouting.CampExpansions
import Planning.Predicates.Compound.{And, Check}
import Planning.Predicates.Economy.{GasAtLeast, GasAtMost, MineralsAtLeast}
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeComplete, UpgradeStarted}
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, StartPositionsAtMost}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchMobileFlying, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Zerg.ZvPHydraRush

class ZvPHydraRush extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(ZvPHydraRush)
  override def scoutPlan: Plan = new ScoutSafelyWithOverlord
  
  override def priorityAttackPlan: Plan = new If(
    new UnitsAtLeast(13, UnitMatchAnd(UnitMatchMobileFlying, UnitMatchWarriors)),
    new CampExpansions)
  
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
      new BuildOrder(Get(6, Zerg.Zergling))),
    new BuildOrder(Get(12, Zerg.Drone)))
  
  private class TransitionToMutalisks extends Parallel(
    new Pump(Zerg.Drone, 33),
    new If(new UnitsAtLeast(20, Zerg.Drone), new BuildGasPumps(2)),
    new Build(
      Get(Zerg.Lair),
      Get(Zerg.Spire)))
  
  override def buildPlans: Seq[Plan] = Seq(new Trigger(new OverpoolSpendLarva, new Parallel(
    new EjectScout,
    new If(
      new And(
        new UnitsAtLeast(24, Zerg.Drone),
        new UnitsAtLeast(3, Zerg.Hatchery)),
      new If(
        new UnitsAtLeast(1, Zerg.Lair),
        new CapGasAtRatioToMinerals(1.0, 50),
        new If(
          new UpgradeStarted(Zerg.HydraliskRange),
          new CapGasAt(100),
          new If(
            new UnitsAtLeast(1, Zerg.HydraliskDen),
            new CapGasAt(175),
            new If(
              new UpgradeStarted(Zerg.ZerglingSpeed),
              new CapGasAt(50),
              new CapGasAt(100)))))),
    new OverpoolBuildLarvaOrDrones,
    new TrainJustEnoughScourge,
    new Trigger(new UnitsAtLeast(6, Zerg.Mutalisk), new UpgradeContinuously(Zerg.AirArmor)),
    new PumpMutalisks,
    new UpgradeContinuously(Zerg.HydraliskSpeed),
    new If(
      new UpgradeComplete(Zerg.HydraliskSpeed),
      new UpgradeContinuously(Zerg.HydraliskRange)),
    new If(
      new UnitsAtLeast(1, Zerg.HydraliskDen, complete = true),
      new PumpJustEnoughHydralisks,
      new PumpJustEnoughZerglings),
    new Trigger(new GasAtLeast(75), new UpgradeContinuously(Zerg.ZerglingSpeed)),
    new Trigger(new UpgradeStarted(Zerg.ZerglingSpeed), new Build(Get(Zerg.HydraliskDen))),
    new If(new UnitsAtLeast(24, Zerg.Drone), new BuildGasPumps),
    new If(new MineralsAtLeast(600), new BuildGasPumps),
    
    // Transition to Mutalisks
    new If(
      new Or(
        new EnemiesAtLeast(4, Protoss.PhotonCannon, complete = true),
        new UnitsAtLeast(20, UnitMatchWarriors)),
      new TransitionToMutalisks),
  
    new Trigger(
      new Or(
        new EnemyBasesAtLeast(2),
        new TwoBaseProtoss),
  
      // Vs. two base
      new Parallel(
        new Pump(Zerg.Drone, 13),
        new RequireMiningBases(3),
        new Trigger(new UnitsAtLeast(13, Zerg.Drone, countEggs = true), new BuildGasPumps(1)),
        new Trigger(new UnitsAtLeast(1, Zerg.Extractor, complete = true), new Build(Get(Zerg.HydraliskDen))),
        new Trigger(new UnitsAtLeast(18, Zerg.Drone, countEggs = true), new BuildGasPumps(2)),
        new Pump(Zerg.Drone, 23),
        new Pump(Zerg.Hydralisk),
        new Pump(Zerg.Drone, 33)
      ),
      
      // Vs. one base
      new Parallel(
        new BuildGasPumps(1),
        new Pump(Zerg.Drone, 9),
        new Pump(Zerg.Drone, 13),
        new RequireMiningBases(3),
        new Pump(Zerg.Drone, 19),
        new If(
          new And(
            new GasAtMost(10),
            new MineralsAtLeast(150)),
          new Pump(Zerg.Zergling)),
        new Pump(Zerg.Hydralisk),
        new Pump(Zerg.Zergling)
      )),
      
    new Trigger(
      new UnitsAtLeast(12, Zerg.Hydralisk),
      new Parallel(
        new RequireBases(5),
        new TransitionToMutalisks))
  )))
}
