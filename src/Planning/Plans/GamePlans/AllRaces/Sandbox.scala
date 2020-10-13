package Planning.Plans.GamePlans.AllRaces

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanTemplate {

  override def attackPlan: Plan = new Attack

  override def buildPlans: Seq[Plan] = Seq(
    new Build(
      Get(2, Protoss.Nexus),
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Stargate),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.FleetBeacon),
      Get(Protoss.Observatory),
      Get(Protoss.RoboticsSupportBay),
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives),
      Get(2, Protoss.Gateway)),
    new BuildGasPumps,
    new Pump(Protoss.Observer, maximumTotal = 1),
    new Pump(Protoss.Shuttle, maximumTotal = 1),
    new Pump(Protoss.Reaver, maximumTotal = 2),
    new Pump(Protoss.Scout, maximumTotal = 1),
    new Pump(Protoss.Corsair, maximumTotal = 1),
    new Pump(Protoss.Carrier),
    new Pump(Protoss.Dragoon, maximumConcurrently = 1),
    new Pump(Protoss.Zealot, maximumConcurrently = 1),
    new Pump(Protoss.HighTemplar, maximumConcurrently = 1),
    new Pump(Protoss.Dragoon),
    new Build(Get(6, Protoss.Gateway))
  )
}
