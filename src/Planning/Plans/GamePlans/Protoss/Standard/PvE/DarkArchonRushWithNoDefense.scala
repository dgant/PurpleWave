package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Macro.BuildRequests._
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{ConsiderAttacking, DefendZones, RecruitFreelancers}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireEssentials}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Protoss.MeldDarkArchons
import Planning.Plans.Placement.BuildCannonsAtBases
import Planning.Predicates.Milestones.{IfOnMiningBases, UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Protoss

class DarkArchonRushWithNoDefense extends Parallel {
  
  children.set(Vector(
    new MeldDarkArchons,
    new RequireEssentials,
    new Build(ProtossBuilds.PvT13Nexus_GateCore: _*),
    new RequireMiningBases(2),
    new RequireSufficientSupply,
    new PumpWorkers,
    new Build(
      Get(1, Protoss.Gateway),
      Get(2, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(4, Protoss.Gateway),
      Get(Protoss.DarkArchonEnergy),
      Get(6, Protoss.Gateway),
      Get(Protoss.MindControl)),
    new If(
      new UnitsAtMost(12, Protoss.DarkArchon),
      new Pump(Protoss.DarkTemplar),
      new Parallel(
        new Build(
          Get(3, Protoss.RoboticsFacility),
          Get(1, Protoss.Observatory),
          Get(1, Protoss.Observer),
          Get(1, Protoss.RoboticsSupportBay),
          Get(1, Protoss.Dragoon),
          Get(Protoss.ScarabDamage)),
        new Pump(Protoss.Reaver))),
    new RequireMiningBases(3),
    new Build(Get(10, Protoss.Gateway)),
    new BuildGasPumps,
    new RequireMiningBases(4),
    new BuildCannonsAtBases(1),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new IfOnMiningBases(5, new Build(Get(14, Protoss.Gateway))),
    new IfOnMiningBases(6, new Build(Get(20, Protoss.Gateway))),
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
