package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Compound.{And, Check}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Zerg

class TwoHatchHydra extends GameplanModeTemplate {
  
  override def aggression: Double = 1.25
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(1, Zerg.SpawningPool),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling),
    Get(2, Zerg.Hatchery),
    Get(1, Zerg.Extractor),
    Get(12, Zerg.Zergling))
  
  override def defaultAttackPlan: Plan = new Attack
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAt(200),
    new If(
      new And(
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1)),
        new UnitsAtMost(1, Zerg.Hatchery)),
      new CapGasAt(0),
      new If(
        new UnitsAtMost(0, Zerg.HydraliskDen),
        new CapGasAt(100),
        new If(
          new UnitsAtMost(0, Zerg.Lair),
          new CapGasAt(175),
          new CapGasAt(225)))),
    new If(
      new Check(() => With.units.countOurs(UnitMatchWarriors) < 8 * With.geography.ourBases.size),
      new PumpWorkers),
    new Pump(Zerg.Lurker, 8, 2),
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
      new Pump(Zerg.Zergling),
      new Pump(Zerg.Hydralisk)),
    new Build(
      Get(1, Zerg.Extractor),
      Get(1, Zerg.HydraliskDen)),
    new UpgradeContinuously(Zerg.HydraliskSpeed),
    new UpgradeContinuously(Zerg.HydraliskRange),
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new BuildGasPumps,
        new Build(
          Get(1, Zerg.Lair),
          Get(Zerg.LurkerMorph))),
      new RequireMiningBases(3)),
    new Build(Get(Zerg.Burrow)),
    new UpgradeContinuously(Zerg.GroundRangeDamage),
    new Build(Get(1, Zerg.EvolutionChamber)),
    new RequireMiningBases(4),
    new Build(Get(2, Zerg.EvolutionChamber)),
    new UpgradeContinuously(Zerg.GroundRangeDamage),
    new UpgradeContinuously(Zerg.GroundArmor),
    new RequireMiningBases(5),
    new Build(Get(1, Zerg.Hive)),
    new RequireMiningBases(8)
  )
}
