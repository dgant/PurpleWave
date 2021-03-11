package Planning.Plans.GamePlans.AllRaces

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Basic.NoPlan
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, Friendly, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanTemplate {

  override def attackPlan: Plan = NoPlan()

  override def buildPlans: Seq[Plan] = Seq(
    new CapGasAt(200),
    new BuildOrder(
      Get(Protoss.Gateway),
      Get(2, Protoss.Pylon),
      Get(Protoss.Zealot),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(2, Protoss.Zealot),
      Get(Protoss.DragoonRange),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory),
      Get(4, Protoss.Gateway)),
    new Pump(Protoss.Observer, 1),
    new PumpRatio(Protoss.Zealot, 0, 12, Seq(Friendly(Protoss.Dragoon, 0.5))),
    new Pump(Protoss.Dragoon),
    new RequireMiningBases(2),
    new Build(Get(8, Protoss.Gateway)))
}
