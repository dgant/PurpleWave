package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.BuildRequests._
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtBases, MeldDarkArchons}
import Planning.Plans.Predicates.Milestones.{IfOnMiningBases, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Recruitment.RecruitFreelancers
import ProxyBwapi.Races.Protoss

class DarkArchonRushWithNoDefense extends Parallel {
  
  children.set(Vector(
    new MeldDarkArchons,
    new RequireEssentials,
    new Build(ProtossBuilds.Opening13Nexus_NoZealot_OneGateCore: _*),
    new RequireMiningBases(2),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new Build(
      GetAtLeast(1, Protoss.Gateway),
      GetAtLeast(2, Protoss.Assimilator),
      GetAtLeast(1, Protoss.CyberneticsCore),
      GetAtLeast(1, Protoss.CitadelOfAdun),
      GetAtLeast(1, Protoss.TemplarArchives),
      GetAtLeast(4, Protoss.Gateway),
      GetUpgrade(Protoss.DarkArchonEnergy),
      GetAtLeast(6, Protoss.Gateway),
      GetTech(Protoss.MindControl)),
    new If(
      new UnitsAtMost(12, Protoss.DarkArchon),
      new TrainContinuously(Protoss.DarkTemplar),
      new Parallel(
        new Build(
          GetAtLeast(3, Protoss.RoboticsFacility),
          GetAtLeast(1, Protoss.Observatory),
          GetAtLeast(1, Protoss.Observer),
          GetAtLeast(1, Protoss.RoboticsSupportBay),
          GetAtLeast(1, Protoss.Dragoon),
          GetUpgrade(Protoss.ScarabDamage)),
        new TrainContinuously(Protoss.Reaver))),
    new RequireMiningBases(3),
    new Build(GetAtLeast(10, Protoss.Gateway)),
    new BuildGasPumps,
    new RequireMiningBases(4),
    new BuildCannonsAtBases(1),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new IfOnMiningBases(5, new Build(GetAtLeast(14, Protoss.Gateway))),
    new IfOnMiningBases(6, new Build(GetAtLeast(20, Protoss.Gateway))),
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
