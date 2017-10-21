package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgradeNext}
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.FindExpansions
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
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.FleetBeacon),
    RequestUpgradeNext(Protoss.AirDamage),
    RequestUpgradeNext(Protoss.CarrierCapacity),
    RequestAtLeast(1, Protoss.Forge)
  )
  
  private class TechToArbiters extends Build(
    RequestAtLeast(1, Protoss.Gateway),
    RequestAtLeast(1, Protoss.CyberneticsCore),
    RequestAtLeast(1, Protoss.CitadelOfAdun),
    RequestAtLeast(1, Protoss.Stargate),
    RequestAtLeast(1, Protoss.TemplarArchives),
    RequestAtLeast(1, Protoss.ArbiterTribunal)
  )
  
  private class TechToObservers extends Build(
    RequestAtLeast(1, Protoss.RoboticsFacility),
    RequestAtLeast(1, Protoss.Observatory)
  )
  
  private class SpamUpgrades extends Parallel(
    new Build(RequestAtLeast(2, Protoss.CyberneticsCore)),
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
      new Build(RequestTech(Protoss.Stasis))),
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
    new OnGasBases(1, new Build(RequestAtLeast(3, Protoss.Stargate))),
    new OnGasBases(2, new Build(RequestAtLeast(5, Protoss.Stargate))),
    new OnGasBases(3, new Build(RequestAtLeast(8, Protoss.Stargate))),
    new OnGasBases(4, new Build(RequestAtLeast(12, Protoss.Stargate))),
    new Build(
      RequestAnother(6, Protoss.PhotonCannon),
      RequestAnother(2, Protoss.Pylon)),
    new ExpandOverIsland(12),
    new FindExpansions { scouts.get.unitMatcher.set(Protoss.Scout) },
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
