package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.{FoundEnemyBase, Scout}
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, GasForUpgrade, UnitsAtLeast}
import Planning.Predicates.Strategy.StartPositionsAtLeast
import Planning.UnitMatchers.UnitMatchOr
import ProxyBwapi.Races.{Terran, Zerg}

class ZvT7Pool extends GameplanTemplate {

  override def attackPlan: Plan = new Attack

  override def scoutPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new If(
      new And(
        new StartPositionsAtLeast(3),
        new Not(new FoundEnemyBase)),
      new Trigger(
        new And(
          new UnitsAtLeast(2, Zerg.Overlord, countEggs = true),
          new UnitsAtLeast(8, Zerg.Drone, countEggs = true)),
        new Scout)))

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(7, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(8, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(9, Zerg.Drone),
    Get(8, Zerg.Zergling),
    Get(2, Zerg.Hatchery),
    Get(14, Zerg.Zergling),
    Get(Zerg.Extractor))

  override def buildPlans: Seq[Plan] = Seq(
    new Write(With.blackboard.pushKiters, true),
    new AllIn(new EnemiesAtLeast(1, UnitMatchOr(Terran.Vulture, Terran.Factory), complete = true)),
    new If(
      new GasForUpgrade(Zerg.ZerglingSpeed),
      new CapGasWorkersAt(0),
      new CapGasAt(100)),
    new If(
      new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
      new Build(Get(Zerg.ZerglingSpeed))),
    new Pump(Zerg.Drone, 6),
    new Pump(Zerg.Zergling),
    new RequireBases(3)
  )
}
