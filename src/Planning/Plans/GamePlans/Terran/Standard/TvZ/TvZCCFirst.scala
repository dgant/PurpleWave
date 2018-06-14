package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.{Plan, Predicate}
import Planning.Plans.Compound.Latch
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZEarlyCCFirst

class TvZCCFirst extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvZEarlyCCFirst)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Terran.Barracks))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override val buildOrder = Vector(
    Get(9,   Terran.SCV),
    Get(1,   Terran.SupplyDepot),
    Get(14,  Terran.SCV),
    Get(2,   Terran.CommandCenter),
    Get(15,  Terran.SCV),
    Get(1,   Terran.Barracks),
    Get(2,   Terran.SupplyDepot),
    Get(17,  Terran.SCV),
    Get(2,   Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Terran.Marine),
    new Build(Get(5, Terran.Barracks))
  )
}
