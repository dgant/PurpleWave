package Planning.Plans.GamePlans

import Macro.BuildRequests._
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.Parallel
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic.{BuildEnoughPylons, TrainContinuously, TrainGatewayUnitsContinuously, TrainProbesContinuously}
import Planning.Plans.Macro.BuildOrders.ScheduleBuildOrder
import ProxyBwapi.Races.Protoss

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  // http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  
  
  val _buildStart = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.Nexus),
    new RequestUnitAnother(9, Protoss.Probe),
    new RequestUnitAtLeast(1, Protoss.Pylon),
    new RequestUnitAtLeast(2, Protoss.Gateway)
  )
  
  val _buildMiddle = Vector[BuildRequest] (
    new RequestUnitAtLeast(1, Protoss.Assimilator),
    new RequestUnitAtLeast(1, Protoss.CyberneticsCore),
    new RequestUpgrade(Protoss.DragoonRange),
    new RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    new RequestUnitAtLeast(2, Protoss.Nexus)
  )
  
  val _twoBaseBuild = Vector[BuildRequest] (
    new RequestUnitAtLeast(2, Protoss.Assimilator),
    new RequestUnitAtLeast(1, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(6, Protoss.Gateway),
    new RequestUnitAtLeast(1, Protoss.RoboticsSupportBay),
    new RequestUnitAtLeast(3, Protoss.Nexus),
    new RequestUnitAtLeast(3, Protoss.Assimilator),
    new RequestUpgrade(Protoss.ScarabDamage),
    new RequestUnitAtLeast(3, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Nexus),
    new RequestUnitAtLeast(4, Protoss.RoboticsFacility),
    new RequestUnitAtLeast(4, Protoss.Assimilator),
    new RequestUnitAtLeast(8, Protoss.Gateway),
    new RequestUnitAtLeast(5, Protoss.Nexus),
    new RequestUnitAtLeast(5, Protoss.Assimilator),
    new RequestUnitAtLeast(12, Protoss.Gateway)
  )
  
  children.set(Vector(
    new ScheduleBuildOrder(_buildStart),
    new BuildEnoughPylons,
    new TrainProbesContinuously,
    new TrainContinuously(Protoss.Reaver),
    new TrainGatewayUnitsContinuously,
    new ScheduleBuildOrder(_buildMiddle),
    new ScheduleBuildOrder(_twoBaseBuild),
    new ScoutAt(9),
    new Attack
  ))
}
