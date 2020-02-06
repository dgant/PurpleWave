package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout, RecruitFreelancers}
import Planning.Plans.Basic.{NoPlan, Write}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstRush
import Planning.Plans.GamePlans.Terran.Standard.TvZ.TvZIdeas.TvZFourPoolEmergency
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.Predicates.Compound.{Latch, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.Employing
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvE2RaxSCVMarine

class TvE2RaxSCVMarine extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvE2RaxSCVMarine)

  override def scoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutAt(10))

  class ReadyToAttack extends Latch(new UnitsAtLeast(8, Terran.Marine))

  override def aggressionPlan: Plan = new If(
    new ReadyToAttack,
    new Aggression(2.0),
    super.aggressionPlan)
  override def attackPlan: Plan = new If(
    new ReadyToAttack,
    new Parallel(
      new Attack,
      new Attack(UnitMatchWorkers, new UnitCountExcept(5, UnitMatchWorkers))))

  override def supplyPlan: Plan = new If(
    new Or(
      new UnitsAtMost(2, Terran.SupplyDepot),
      new MineralsAtLeast(400)),
    super.supplyPlan)

  override def workerPlan: Plan = NoPlan()

  override def emergencyPlans: Seq[Plan] = Seq(
    new TvZFourPoolEmergency,
    new TerranReactionVsWorkerRush
  )
  
  override val buildOrder = Vector(
    Get(9, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(10, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(11, Terran.SCV),
    Get(2, Terran.Barracks),
    Get(13, Terran.SCV),
    Get(1, Terran.Marine),
    Get(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Write(With.blackboard.pushKiters, true),
    new DefendFightersAgainstRush,
    new Pump(Terran.Marine),
    new Pump(Terran.SCV),
    new If(
      new ReadyToAttack,
      new RecruitFreelancers(UnitMatchWorkers, new UnitCountExcept(5, UnitMatchWorkers)),
      new EjectScout)
  )
}
