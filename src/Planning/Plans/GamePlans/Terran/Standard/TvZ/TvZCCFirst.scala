package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvZ.TvZEarly1RaxFEConservative

class TvZCCFirst extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvZEarly1RaxFEConservative)
  override val completionCriteria: Plan = new Latch(new UnitsAtLeast(2, Terran.Barracks))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override val buildOrder = Vector(
    RequestAtLeast(1,   Terran.CommandCenter),
    RequestAtLeast(9,   Terran.SCV),
    RequestAtLeast(1,   Terran.SupplyDepot),
    RequestAtLeast(14,  Terran.SCV),
    RequestAtLeast(2,   Terran.CommandCenter),
    RequestAtLeast(15,  Terran.SCV),
    RequestAtLeast(1,   Terran.Barracks),
    RequestAtLeast(2,   Terran.SupplyDepot),
    RequestAtLeast(17,  Terran.SCV),
    RequestAtLeast(2,   Terran.Barracks))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Marine),
    new Build(RequestAtLeast(5, Terran.Barracks))
  )
}
