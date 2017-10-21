package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1015GateReaverExpand

class PvPOpen1015GateGoonReaverExpand extends GameplanModeTemplatePvP {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1015GateReaverExpand)
  override val completionCriteria : Plan      = new OnMiningBases(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val buildOrder: Seq[BuildRequest] =
    ProtossBuilds.Opening10Gate15GateDragoons ++ Vector(RequestAtLeast(5, Protoss.Dragoon))
  
  override def buildPlans = Vector(
    new TrainContinuously(Protoss.Reaver, 1),
    new FlipIf(
      new UnitsAtLeast(5, Protoss.Dragoon),
      new TrainContinuously(Protoss.Dragoon),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay)),
        new RequireMiningBases(2)
      )))
}
