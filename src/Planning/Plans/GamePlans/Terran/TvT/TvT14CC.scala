package Planning.Plans.GamePlans.Terran.TvT

import Lifecycle.With
import Macro.Requests.{Get, RequestBuildable}
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Placement.BuildBunkersAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Predicate
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT14CC

class TvT14CC extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvT14CC)
  override val completionCriteria: Predicate = new Latch(new And(new UnitsAtLeast(1, Terran.Bunker, complete = true), new UnitsAtLeast(2, Terran.Refinery)))

  override def scoutPlan = new ScoutOn(Terran.CommandCenter, quantity = 2)
  override def attackPlan: Plan = NoPlan()

  override def buildOrder: Seq[RequestBuildable] = Seq(
    Get(9, Terran.SCV),
    Get(Terran.SupplyDepot),
    Get(14, Terran.SCV),
    Get(2, Terran.CommandCenter),
    Get(15, Terran.SCV),
    Get(Terran.Barracks),
    Get(16, Terran.SCV),
    Get(Terran.Refinery),
    Get(2, Terran.SupplyDepot),
    Get(20, Terran.SCV),
    Get(Terran.Marine),
    Get(Terran.Factory),
    Get(22, Terran.SCV),
    Get(2, Terran.Marine))

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new Not(new EnemyStrategy(With.fingerprints.fourteenCC)),
      new Parallel(
        new BuildBunkersAtNatural(1),
        new Build(Get(Terran.Bunker)))), // Forces completion of the Bunker
    new BuildGasPumps,
    new Pump(Terran.Marine),
    new Pump(Terran.Vulture)
  )
}
