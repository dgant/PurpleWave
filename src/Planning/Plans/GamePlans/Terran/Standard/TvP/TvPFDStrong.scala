package Planning.Plans.GamePlans.Terran.Standard.TvP

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plans.Army.{AttackAndHarass, ConsiderAttacking}
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.ScoutAt
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{BasesAtLeast, EnemiesAtLeast, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.MatchTank
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvPFDStrong

class TvPFDStrong extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvPFDStrong)
  override val completionCriteria: Predicate = new Latch(new BasesAtLeast(2))

  override def scoutPlan: Plan = new ScoutAt(13)
  override def attackPlan: Plan = new Trigger(
    new UnitsAtLeast(2, MatchTank, complete = true),
    new If(
      new EnemiesAtLeast(2, Protoss.Gateway),
      new ConsiderAttacking,
      new AttackAndHarass))
  
  override def emergencyPlans: Seq[Plan] = super.emergencyPlans ++
    TvPIdeas.emergencyPlans
  
  override def workerPlan: Plan = TvPIdeas.workerPlan

  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(
      Get(10, Terran.SCV),
      Get(Terran.SupplyDepot)),
    new Trigger(
      new MineralsAtLeast(250), // Keep that worker mining as long as possible
      new BuildOrder(
        Get(Terran.Barracks),
        Get(Terran.Refinery)),
      // Don't build this, just hold minerals for it
      new BuildOrder(Get(2, Terran.CommandCenter))),
    new BuildOrder(
      Get(14, Terran.SCV),
      Get(Terran.Factory),
      Get(15, Terran.SCV),
      Get(2,  Terran.SupplyDepot),
      Get(1,  Terran.Marine),
      Get(16, Terran.SCV),
      Get(2,  Terran.Marine),
      Get(17, Terran.SCV),
      Get(3,  Terran.SupplyDepot)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TvPIdeas.CutGasDuringFactory,
    new TvPIdeas.ReactiveEarlyVulture,
    new Pump(Terran.MachineShop, 1),
    new BuildOrder(Get(2, Terran.SiegeTankUnsieged)),
    new Pump(Terran.Marine),
    new Build(Get(Terran.SpiderMinePlant)),
    new Pump(Terran.Vulture),
    new Build(Get(Terran.SiegeMode)),
    new If(
      new EnemyStrategy(With.fingerprints.twoGate),
      new Build(Get(2, Terran.Factory))),
    new RequireBases(2)
  )
}
