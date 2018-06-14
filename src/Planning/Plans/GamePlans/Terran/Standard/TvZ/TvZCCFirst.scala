package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.GetAtLeast
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZEarlyCCFirst

class TvZCCFirst extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvZEarlyCCFirst)
  override val completionCriteria: Plan = new Latch(new UnitsAtLeast(2, Terran.Barracks))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override val buildOrder = Vector(
    GetAtLeast(9,   Terran.SCV),
    GetAtLeast(1,   Terran.SupplyDepot),
    GetAtLeast(14,  Terran.SCV),
    GetAtLeast(2,   Terran.CommandCenter),
    GetAtLeast(15,  Terran.SCV),
    GetAtLeast(1,   Terran.Barracks),
    GetAtLeast(2,   Terran.SupplyDepot),
    GetAtLeast(17,  Terran.SCV),
    GetAtLeast(2,   Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Marine),
    new Build(GetAtLeast(5, Terran.Barracks))
  )
}
