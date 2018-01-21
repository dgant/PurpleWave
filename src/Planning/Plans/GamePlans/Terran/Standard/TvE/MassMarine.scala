package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEMassMarine

class MassMarine extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassMarine)
  
  override val buildOrder = Vector(
    RequestAtLeast(1, Terran.CommandCenter),
    RequestAtLeast(8, Terran.SCV),
    RequestAtLeast(2, Terran.Barracks),
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(1, Terran.Marine))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.SCV),
    new TrainContinuously(Terran.Barracks)
  )
}