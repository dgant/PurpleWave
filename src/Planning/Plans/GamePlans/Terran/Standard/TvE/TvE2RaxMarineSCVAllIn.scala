package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountExcept
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvESCVMarineAllIn

class TvE2RaxMarineSCVAllIn extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvESCVMarineAllIn)
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new ScoutAt(10))
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(8, Terran.Marine),
    new Parallel(
      new Aggression(2.0),
      new Attack,
      new Attack {
        attackers.get.unitCounter.set(new UnitCountExcept(5, UnitMatchWorkers))
        attackers.get.unitMatcher.set(UnitMatchWorkers)
      }
    )
  )
  
  override def defaultSupplyPlan: Plan = new If(
    new Or(
      new UnitsAtMost(2, Terran.SupplyDepot),
      new MineralsAtLeast(400)),
    super.defaultSupplyPlan)
  
  override def defaultWorkerPlan: Plan = NoPlan()
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(10, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(11, Terran.SCV),
    RequestAtLeast(2, Terran.Barracks),
    RequestAtLeast(13, Terran.SCV),
    RequestAtLeast(1, Terran.Marine),
    RequestAtLeast(2, Terran.SupplyDepot))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.SCV)
  )
}
