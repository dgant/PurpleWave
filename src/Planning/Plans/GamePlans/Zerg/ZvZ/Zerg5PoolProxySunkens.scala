package Planning.Plans.GamePlans.Zerg.ZvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Check}
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.{Do, NoPlan}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Milestones.{FrameAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Strategy.Employing
import Planning.UnitCounters.UnitCountEverything
import Planning.{Plan, Predicate, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.FivePoolProxySunkens

class Zerg5PoolProxySunkens extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(FivePoolProxySunkens)
  
  override def overlordPlan: Plan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Attack
  override def scoutPlan: Plan = new Scout {
    scouts.get.unitMatcher.set(Zerg.Overlord)
    scouts.get.unitCounter.set(UnitCountEverything)
  }
  
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
  
  override val buildOrder = Vector(
    Get(1, Zerg.Hatchery),
    Get(5, Zerg.Drone),
    Get(1, Zerg.SpawningPool),
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
            new Scout(1),
            new Scout(1)))),
    new Pump(Zerg.Zergling)
  )
}
