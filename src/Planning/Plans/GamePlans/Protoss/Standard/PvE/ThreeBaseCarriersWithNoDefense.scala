package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.{Get, GetAnother, NextUpgrade}
import Planning.Plans.Army.{Attack, ConsiderAttacking, DefendZones, RecruitFreelancers}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Predicates.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import ProxyBwapi.Races.Protoss

class ThreeBaseCarriersWithNoDefense extends Parallel {
  
  private class ExpandOverIsland(maxBases: Int) extends RequireMiningBases {
    description.set("Fill island with expansions")
    basesDesired.set(
      if (With.strategy.isPlasma)
        3
      else
        Math.min(maxBases, With.geography.bases.count(_.zone.canWalkTo(With.geography.ourMain.zone))))
  }
  
  private class TechToCarriers extends Build(
    Get(1, Protoss.Gateway),
    Get(1, Protoss.CyberneticsCore),
    Get(1, Protoss.Stargate),
    Get(1, Protoss.FleetBeacon),
    NextUpgrade(Protoss.AirDamage),
    NextUpgrade(Protoss.CarrierCapacity),
    Get(1, Protoss.Forge)
  )
  
  private class TechToArbiters extends Build(
    Get(1, Protoss.Gateway),
    Get(1, Protoss.CyberneticsCore),
    Get(1, Protoss.CitadelOfAdun),
    Get(1, Protoss.Stargate),
    Get(1, Protoss.TemplarArchives),
    Get(1, Protoss.ArbiterTribunal)
  )
  
  private class TechToObservers extends Build(
    Get(1, Protoss.RoboticsFacility),
    Get(1, Protoss.Observatory)
  )
  
  private class SpamUpgrades extends Parallel(
    new Build(Get(2, Protoss.CyberneticsCore)),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor))
  
  children.set(Vector(
    new Build(ProtossBuilds.PvT13Nexus_NZ1GateCore: _*),
    new RequireSufficientSupply,
    new PumpWorkers,
    new BuildGasPumps,
    new ExpandOverIsland(3),
    new TechToCarriers,
    new If(
      new UnitsAtLeast(1, Protoss.Arbiter),
      new Build(Get(Protoss.Stasis))),
    new If(
      new UnitsAtLeast(12, Protoss.Carrier),
      new Parallel(
        new IfOnMiningBases(2, new Parallel(new SpamUpgrades, new TechToObservers)),
        new IfOnMiningBases(3, new Parallel(new TechToArbiters)),
        new Pump(Protoss.Observer, 2),
        new Pump(Protoss.Arbiter, 2),
        new Pump(Protoss.Carrier)),
      new Parallel(
        new Pump(Protoss.Scout, 1),
        new Pump(Protoss.Carrier),
        new Pump(Protoss.Observer, 1))),
    new OnGasPumps(1, new Build(Get(3, Protoss.Stargate))),
    new OnGasPumps(2, new Build(Get(5, Protoss.Stargate))),
    new OnGasPumps(3, new Build(Get(8, Protoss.Stargate))),
    new OnGasPumps(4, new Build(Get(12, Protoss.Stargate))),
    new Build(
      GetAnother(6, Protoss.PhotonCannon),
      GetAnother(2, Protoss.Pylon)),
    new ExpandOverIsland(12),
    new Attack(Protoss.Scout), // TODO: Scout expansions, don't attack
    new DefendZones,
    new If(
      new UnitsAtLeast(8 * 8, Protoss.Interceptor),
      new ConsiderAttacking),
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(30),
    new Gather,
    new RecruitFreelancers
  ))
}
