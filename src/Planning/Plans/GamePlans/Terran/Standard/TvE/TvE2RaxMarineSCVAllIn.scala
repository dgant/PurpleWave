package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.Get
import Planning.Predicates.Compound.Not
import Planning.UnitCounters.UnitCountExcept
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.{Plan, Predicate}
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Employing
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvESCVMarineAllIn

class TvE2RaxMarineSCVAllIn extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvESCVMarineAllIn)
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutAt(10))
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(8, Terran.Marine),
    new Parallel(
      new Aggression(2.0),
      new Attack,
      new Attack(UnitMatchWorkers, new UnitCountExcept(5, UnitMatchWorkers))))
  
  override def defaultSupplyPlan: Plan = new If(
    new Or(
      new UnitsAtMost(2, Terran.SupplyDepot),
      new MineralsAtLeast(400)),
    super.defaultSupplyPlan)
  
  override def defaultWorkerPlan: Plan = NoPlan()
  
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
    new Pump(Terran.Marine),
    new Pump(Terran.SCV)
  )
}
