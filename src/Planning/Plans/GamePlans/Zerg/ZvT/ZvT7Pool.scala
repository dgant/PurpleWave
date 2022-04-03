package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Buildables.{RequestProduction, Get}
import Planning.Plans.Army.{AllInIf, AttackAndHarass}
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.ScoutNow
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.UnitMatchers.MatchOr
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT7Pool

class ZvT7Pool extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT7Pool)
  override val completionCriteria: Predicate = new BasesAtLeast(2)

  override def attackPlan: Plan = new AttackAndHarass

  override def scoutPlan: Plan = new If(
    new And(
      new StartPositionsAtLeast(3),
      new Not(new FoundEnemyBase)),
    new Trigger(
      new And(
        new UnitsAtLeast(2, Zerg.Overlord),
        new UnitsAtLeast(8, Zerg.Drone)),
      new ScoutNow))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[RequestProduction] = Seq(
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
    new AllInIf(new EnemiesAtLeast(1, MatchOr(Terran.Vulture, Terran.Factory), complete = true)),
    new If(
      new GasForUpgrade(Zerg.ZerglingSpeed),
      new CapGasWorkersAt(0),
      new CapGasAt(100)),
    new If(
      new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
      new Build(Get(Zerg.ZerglingSpeed))),
    new Pump(Zerg.Drone, 6),
    new Pump(Zerg.Zergling),
    new RequireBases(2)
  )
}
