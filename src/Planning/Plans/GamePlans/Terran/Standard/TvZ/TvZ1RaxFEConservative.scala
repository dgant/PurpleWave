package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.Predicates.Compound.{And, Latch}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.FlipIf
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZEarly1RaxFEConservative

class TvZ1RaxFEConservative extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvZEarly1RaxFEConservative)
  override val completionCriteria: Predicate = new Latch(new MiningBasesAtLeast(2))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override val buildOrder = Vector(
    Get(1,   Terran.CommandCenter),
    Get(9,   Terran.SCV),
    Get(1,   Terran.Barracks),
    Get(1,   Terran.SupplyDepot),
    Get(11,  Terran.SCV),
    Get(1,   Terran.Marine),
    Get(1,   Terran.Bunker))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Terran.Marine),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(4, UnitMatchWarriors)),
      new Build(Get(4, Terran.Barracks)),
      new RequireMiningBases(2))
  )
}
