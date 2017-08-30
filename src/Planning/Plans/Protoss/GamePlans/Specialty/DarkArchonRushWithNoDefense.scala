package Planning.Plans.Protoss.GamePlans.Specialty

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtBases, BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Recruitment.RecruitFreelancers
import ProxyBwapi.Races.Protoss

class DarkArchonRushWithNoDefense extends Parallel {
  
  children.set(Vector(
    new MeldDarkArchons,
    new RequireBareMinimum,
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*),
    new RequireMiningBases(2),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(4, Protoss.Gateway),
      RequestUpgrade(Protoss.DarkArchonEnergy),
      RequestAtLeast(6, Protoss.Gateway),
      RequestTech(Protoss.MindControl)),
    new If(
      new UnitsAtMost(12, Protoss.DarkArchon),
      new TrainContinuously(Protoss.DarkTemplar),
      new Parallel(
        new Build(
          RequestAtLeast(3, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(1, Protoss.Observer),
          RequestAtLeast(1, Protoss.RoboticsSupportBay),
          RequestAtLeast(1, Protoss.Dragoon),
          RequestUpgrade(Protoss.ScarabDamage)),
        new TrainContinuously(Protoss.Reaver))),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new BuildGasPumps,
    new RequireMiningBases(4),
    new BuildCannonsAtBases(1),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new OnMiningBases(5, new Build(RequestAtLeast(14, Protoss.Gateway))),
    new OnMiningBases(6, new Build(RequestAtLeast(20, Protoss.Gateway))),
    new RequireMiningBases(5),
    new RequireMiningBases(6),
    new UpgradeContinuously(Protoss.Shields),
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new DefendZones,
    new If(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new ConsiderAttacking),
    new RecruitFreelancers
  ))
}
