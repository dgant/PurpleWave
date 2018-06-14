package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetTech}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.SafeAtHome
import ProxyBwapi.Races.Zerg

class TwoHatchHydra extends GameplanModeTemplate {
  
  override def aggression: Double = 1.25
  override def buildOrder: Seq[BuildRequest] = Vector(
    GetAtLeast(9, Zerg.Drone),
    GetAtLeast(2, Zerg.Overlord),
    GetAtLeast(1, Zerg.SpawningPool),
    GetAtLeast(11, Zerg.Drone),
    GetAtLeast(6, Zerg.Zergling),
    GetAtLeast(2, Zerg.Hatchery),
    GetAtLeast(1, Zerg.Extractor),
    GetAtLeast(12, Zerg.Zergling))
  
  override def defaultAttackPlan: Plan = new Attack
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.gasLimitCeiling = 200),
    new If(
      new And(
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1)),
        new UnitsAtMost(1, Zerg.Hatchery)),
      new Do(() => With.blackboard.gasLimitCeiling = 0),
      new If(
        new UnitsAtMost(0, Zerg.HydraliskDen),
        new Do(() => With.blackboard.gasLimitCeiling = 100),
        new If(
          new UnitsAtMost(0, Zerg.Lair),
          new Do(() => With.blackboard.gasLimitCeiling = 175),
          new Do(() => With.blackboard.gasLimitCeiling = 225)))),
    new If(
      new Check(() => With.units.countOurs(UnitMatchWarriors) < 8 * With.geography.ourBases.size),
      new TrainWorkersContinuously),
    new TrainContinuously(Zerg.Lurker, 8, 2),
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
      new TrainContinuously(Zerg.Zergling),
      new TrainContinuously(Zerg.Hydralisk)),
    new Build(
      GetAtLeast(1, Zerg.Extractor),
      GetAtLeast(1, Zerg.HydraliskDen)),
    new UpgradeContinuously(Zerg.HydraliskSpeed),
    new UpgradeContinuously(Zerg.HydraliskRange),
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new BuildGasPumps,
        new Build(
          GetAtLeast(1, Zerg.Lair),
          GetTech(Zerg.LurkerMorph))),
      new RequireMiningBases(3)),
    new Build(GetTech(Zerg.Burrow)),
    new UpgradeContinuously(Zerg.GroundRangeDamage),
    new Build(GetAtLeast(1, Zerg.EvolutionChamber)),
    new RequireMiningBases(4),
    new Build(GetAtLeast(2, Zerg.EvolutionChamber)),
    new UpgradeContinuously(Zerg.GroundRangeDamage),
    new UpgradeContinuously(Zerg.GroundArmor),
    new RequireMiningBases(5),
    new Build(GetAtLeast(1, Zerg.Hive)),
    new RequireMiningBases(8)
  )
}
