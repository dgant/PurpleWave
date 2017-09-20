package Planning.Plans.Protoss.GamePlans.Specialty

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Matchup.EnemyIsTerran
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildCannonsAtNatural, BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.Situational.{Defend2GateAgainst4Pool, DefendAgainstProxy}
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss

class Stove extends Parallel {
  
  val zzCoreZ = Vector (
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Zealot),
    RequestAtLeast(3,   Protoss.Pylon))
  
  val coreFirst = Vector(
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),
    RequestAtLeast(11,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon))
  
  children.set(Vector(
    new RequireEssentials,
    new If(
      new EnemyIsTerran,
      new Build(coreFirst: _*),
      new Build(zzCoreZ: _*)),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new BuildGasPumps,
    new Trigger(
      new UnitsAtLeast(1, Protoss.Scout),
      initialAfter = new Parallel(
        new RequireMiningBases(2))),
    new If(
      new UnitsAtLeast(1, Protoss.ArbiterTribunal),
      new TrainContinuously(Protoss.Arbiter, 1),
      new TrainContinuously(Protoss.DarkTemplar, 2)),
    new If(
      new UnitsAtLeast(1, Protoss.Arbiter, complete = true),
      new TrainContinuously(Protoss.Scout),
      new TrainContinuously(Protoss.Scout, 3)),
    new TrainContinuously(Protoss.Zealot),
    new BuildOrder(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.Scout)),
    new BuildCannonsAtNatural(2),
    new Build(
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(2, Protoss.Nexus),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(1, Protoss.ArbiterTribunal),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(10, Protoss.Gateway)),
    new FirstEightMinutes(new Defend2GateAgainst4Pool),
    new DefendZones,
    new ScoutAt(14),
    new Attack { attackers.get.unitMatcher.set(Protoss.Scout) },
    new ConsiderAttacking,
    new FollowBuildOrder,
    new DefendAgainstProxy,
    new RemoveMineralBlocksAt(50),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}