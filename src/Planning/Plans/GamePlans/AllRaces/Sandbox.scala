package Planning.Plans.GamePlans.AllRaces

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanTemplate {

  override def buildOrder: Seq[BuildRequest] = ProtossBuilds.PvT13Nexus_GateCore

  override def workerPlan: Plan = new PumpWorkers(maximumConcurrently = 2)

  override def buildPlans: Seq[Plan] = Seq(
    //new Build(Get(Protoss.DragoonRange)),
    //new Pump(Protoss.Dragoon),
    new Pump(Protoss.Zealot),
    new RequireMiningBases(5),
    new Build(Get(2, Protoss.Gateway))
  )
}
