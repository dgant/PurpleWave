package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly1GateRange

class PvT1GateRange extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly1GateRange)
  override val completionCriteria = new UnitsAtLeast(6, Protoss.Dragoon)
  override val superSaturate      = true
  override val buildOrder         = ProtossBuilds.Opening1GateRangeExpand
  
  override val buildPlans = Vector (
    new TrainContinuously(Protoss.Dragoon),
    new PvTIdeas.BuildSecondGasIfWeNeedIt,
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observer),
      RequestAtLeast(3, Protoss.Gateway)))
}

