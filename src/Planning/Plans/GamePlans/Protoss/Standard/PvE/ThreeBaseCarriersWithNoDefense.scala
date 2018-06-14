package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.{GetAnother, GetAtLeast, GetTech, GetUpgradeNext}
import Planning.Plans.Army.{Attack, ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import Planning.Plans.Recruitment.RecruitFreelancers
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
    GetAtLeast(1, Protoss.Gateway),
    GetAtLeast(1, Protoss.CyberneticsCore),
    GetAtLeast(1, Protoss.Stargate),
    GetAtLeast(1, Protoss.FleetBeacon),
    GetUpgradeNext(Protoss.AirDamage),
    GetUpgradeNext(Protoss.CarrierCapacity),
    GetAtLeast(1, Protoss.Forge)
  )
  
  private class TechToArbiters extends Build(
    GetAtLeast(1, Protoss.Gateway),
    GetAtLeast(1, Protoss.CyberneticsCore),
    GetAtLeast(1, Protoss.CitadelOfAdun),
    GetAtLeast(1, Protoss.Stargate),
    GetAtLeast(1, Protoss.TemplarArchives),
    GetAtLeast(1, Protoss.ArbiterTribunal)
  )
  
  private class TechToObservers extends Build(
    GetAtLeast(1, Protoss.RoboticsFacility),
    GetAtLeast(1, Protoss.Observatory)
  )
  
  private class SpamUpgrades extends Parallel(
    new Build(GetAtLeast(2, Protoss.CyberneticsCore)),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor))
  
  children.set(Vector(
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new BuildGasPumps,
    new ExpandOverIsland(3),
    new TechToCarriers,
    new If(
      new UnitsAtLeast(1, Protoss.Arbiter),
      new Build(GetTech(Protoss.Stasis))),
    new If(
      new UnitsAtLeast(12, Protoss.Carrier),
      new Parallel(
        new IfOnMiningBases(2, new Parallel(new SpamUpgrades, new TechToObservers)),
        new IfOnMiningBases(3, new Parallel(new TechToArbiters)),
        new TrainContinuously(Protoss.Observer, 2),
        new TrainContinuously(Protoss.Arbiter, 2),
        new TrainContinuously(Protoss.Carrier)),
      new Parallel(
        new TrainContinuously(Protoss.Scout, 1),
        new TrainContinuously(Protoss.Carrier),
        new TrainContinuously(Protoss.Observer, 1))),
    new OnGasPumps(1, new Build(GetAtLeast(3, Protoss.Stargate))),
    new OnGasPumps(2, new Build(GetAtLeast(5, Protoss.Stargate))),
    new OnGasPumps(3, new Build(GetAtLeast(8, Protoss.Stargate))),
    new OnGasPumps(4, new Build(GetAtLeast(12, Protoss.Stargate))),
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
