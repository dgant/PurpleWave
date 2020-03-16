package Planning.Plans.GamePlans.Zerg.ZvZ

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Scouting.ScoutWithWorkers
import Planning.Predicates.Compound.{And, Check}
import Planning.Predicates.Milestones.{FrameAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ5PoolSunkens

class ZvZ5PoolSunkens extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(ZvZ5PoolSunkens)

  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Attack
  override def initialScoutPlan: Plan = NoPlan()
  
  private def blueprintCreepColony: Blueprint = new Blueprint(this,
    building    = Some(Zerg.CreepColony),
    requireZone = ProxyPlanner.proxyEnemyMain,
    placement   = Some(PlacementProfiles.tech))
  
  override def placementPlan: Plan =
    new If(
      new Check(() => ProxyPlanner.proxyEnemyMain.isDefined),
      new ProposePlacement {
        override lazy val blueprints = Vector(
          blueprintCreepColony,
          blueprintCreepColony,
          blueprintCreepColony,
          blueprintCreepColony,
          blueprintCreepColony,
          blueprintCreepColony,
          blueprintCreepColony)
      })


  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override val buildOrder = Vector(
    Get(5, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(6, Zerg.Drone),
    Get(6, Zerg.Zergling))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Pump(Zerg.SunkenColony),
    new If(
      new UnitsAtLeast(4, Zerg.Drone)),
      new Trigger(
        new And(
          new FrameAtLeast(GameTime(2, 30)()),
          new Check(() => With.geography.enemyBases.exists(_.lastScoutedFrame > 0))),
        new Build(Get(2, Zerg.CreepColony)),
        new If(
          new FrameAtLeast(GameTime(1, 10)()),
          new Parallel(
            new ScoutWithWorkers(1),
            new ScoutWithWorkers(1)))),
    new Pump(Zerg.Zergling)
  )
}
