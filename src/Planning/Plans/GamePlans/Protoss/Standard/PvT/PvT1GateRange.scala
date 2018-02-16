package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateRange

class PvT1GateRange extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1GateRange)
  override val completionCriteria = new UnitsAtLeast(1, Protoss.Observatory)
  override val buildOrder         = ProtossBuilds.Opening1GateRangeExpand
  
  override val buildPlans = Vector (
    new TrainContinuously(Protoss.Dragoon),
    new BuildGasPumps,
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observer)))
}

