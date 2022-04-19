package Planning.Plans.GamePlans.Zerg.ZvT

import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.Employing
import Planning.Plan
import Planning.Predicates.Predicate
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvT13PoolMuta

class ZvT13PoolMuta extends GameplanTemplate {

  override val activationCriteria: Predicate = Employing(ZvT13PoolMuta)
  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush)

  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new AttackAndHarass
  
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
