package Planning.Plans.GamePlans.AllRaces

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import ProxyBwapi.Races.Protoss

class Sandbox extends GameplanTemplate {

  //override def priorityAttackPlan: Plan = new Attack

  /*
  override def buildPlans: Seq[Plan] = Seq(
    new RequireMiningBases(2),
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory),
      Get(4, Protoss.Gateway)),
    new Pump(Protoss.Observer, 1),
    new PumpRatio(Protoss.Zealot, 0, 12, Seq(Friendly(Protoss.Dragoon, 0.5))),
    new Pump(Protoss.Dragoon),
    new Build(Get(8, Protoss.Gateway)))*/

  /*
  override def buildPlans: Seq[Plan] = Seq(
    new BuildOrder(
      Get(8, Protoss.Probe),
      Get(Protoss.Pylon),
      Get(10, Protoss.Probe),
      Get(Protoss.Gateway),
      Get(12, Protoss.Probe),
      Get(Protoss.Assimilator),
      Get(13, Protoss.Probe),
      Get(Protoss.Zealot),
      Get(14, Protoss.Probe),
      Get(2, Protoss.Pylon),
      Get(15, Protoss.Probe),
      Get(Protoss.CyberneticsCore),
      Get(16, Protoss.Probe),
      Get(2, Protoss.Zealot),
      Get(18, Protoss.Probe),
      Get(3, Protoss.Pylon),
      Get(19, Protoss.Probe),
      Get(Protoss.Dragoon),
      Get(Protoss.Stargate),
      Get(21, Protoss.Probe),
      Get(2, Protoss.Stargate)),
    new Trigger(
      new UnitsAtLeast(8, Protoss.Scout),
      new RequireMiningBases(2)),
    new BuildGasPumps,
    new Pump(Protoss.Scout),
    new Pump(Protoss.Zealot),
    new RequireMiningBases(2),
    new If(
      new MiningBasesAtLeast(2),
      new Build(
        Get(Protoss.FleetBeacon),
        Get(Protoss.AirDamage),
        Get(Protoss.ScoutSpeed),
        Get(Protoss.AirDamage, 2),
        Get(4, Protoss.Stargate))),
    new RequireMiningBases(3),
    new Build(Get(6, Protoss.Stargate)),
    new RequireMiningBases(4),
    new Build(Get(8, Protoss.Stargate)),
    new RequireMiningBases(5),
    new Build(Get(12, Protoss.Stargate)))
  */

  def buildPlansDiverse: Seq[Plan] = Seq(
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
