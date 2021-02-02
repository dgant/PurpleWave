package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvT13PoolMuta

class ZvT13PoolMuta extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT13PoolMuta)
  override def aggressionPlan: Plan = new Aggression(0.7)
  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush)

  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Attack
  
  override def buildOrderPlan: Plan = new Parallel (
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(13, Zerg.Drone),
      Get(1, Zerg.SpawningPool),
      Get(1, Zerg.Extractor),
      Get(14, Zerg.Drone)),
    new RequireMiningBases(2),
    new BuildOrder(
      Get(1, Zerg.Lair),
      Get(4, Zerg.Zergling),
      Get(15, Zerg.Drone),
      Get(1, Zerg.Spire),
      Get(16, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(1, Zerg.CreepColony),
      initialBefore = new Build(Get(1, Zerg.CreepColony))),
    new BuildOrder(
      Get(3, Zerg.Overlord)))
  
  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(Zerg.CreepColony, requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Zerg.CreepColony, requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(Zerg.CreepColony, requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)))

  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Zerg.Mutalisk),
    new Pump(Zerg.SunkenColony),
    new Pump(Zerg.Drone, 16),
    new Build(Get(5, Zerg.Overlord)),
    new Trigger(
      new UnitsAtLeast(4, Zerg.Overlord, complete = true),
      new BuildGasPumps)
  )
}
