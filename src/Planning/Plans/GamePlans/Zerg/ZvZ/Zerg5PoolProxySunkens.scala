package Planning.Plans.GamePlans.Zerg.ZvZ

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCountEverything
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{FrameAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.FivePoolProxySunkens

class Zerg5PoolProxySunkens extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(FivePoolProxySunkens)
  
  override def defaultOverlordPlan: Plan = NoPlan()
  override def defaultSupplyPlan: Plan = NoPlan()
  override def defaultAttackPlan: Plan = new Attack
  override def defaultScoutPlan: Plan = new Scout {
    scouts.get.unitMatcher.set(Zerg.Overlord)
    scouts.get.unitCounter.set(UnitCountEverything)
  }
  
  private def blueprintCreepColony: Blueprint = new Blueprint(this,
    building    = Some(Zerg.CreepColony),
    requireZone = ProxyPlanner.proxyEnemyMain,
    placement   = Some(PlacementProfiles.tech))
  
  override def defaultPlacementPlan: Plan =
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
    RequestAtLeast(1, Zerg.Hatchery),
    RequestAtLeast(5, Zerg.Drone),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(6, Zerg.Drone),
    RequestAtLeast(6, Zerg.Zergling))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new TrainContinuously(Zerg.SunkenColony),
    new If(
      new UnitsAtLeast(4, Zerg.Drone)),
      new Trigger(
        new And(
          new FrameAtLeast(GameTime(2, 30)()),
          new Check(() => With.geography.enemyBases.exists(_.lastScoutedFrame > 0))),
        new Build(RequestAtLeast(2, Zerg.CreepColony)),
        new If(
          new FrameAtLeast(GameTime(1, 10)()),
          new Parallel(
            new Scout(1),
            new Scout(1)))),
    new TrainContinuously(Zerg.Zergling)
  )
}