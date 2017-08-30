package Planning.Plans.Protoss.GamePlans.Specialty

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtBases, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
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
      new UnitsAtMost(20, Protoss.DarkArchon),
      new TrainContinuously(Protoss.DarkTemplar),
      new Parallel(
        new Build(RequestUpgrade(Protoss.DragoonRange)),
        new TrainContinuously(Protoss.Dragoon))
    ),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new RequireMiningBases(4),
    new BuildCannonsAtBases(1),
    new Build(RequestAnother(4, Protoss.PhotonCannon)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new RequireMiningBases(5),
    new Build(RequestAtLeast(14, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new FollowBuildOrder,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new DefendZones,
    new If(
      new UnitsAtLeast(10, UnitMatchWarriors),
      new ConsiderAttacking),
    new RecruitFreelancers
  ))
}
