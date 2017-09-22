package Planning.Plans.Protoss.GamePlans.Specialty

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Matchup.EnemyIsTerran
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.Situational.{DefendAgainstProxy, DefendZealotsAgainst4Pool}
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss

class TwoBaseStove extends Parallel {
  
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
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),
    RequestAtLeast(2,   Protoss.Pylon),
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),
    RequestAtLeast(1,   Protoss.Stargate))
  
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
      new UnitsAtLeast(1, Protoss.PhotonCannon),
      initialAfter = new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(1, Protoss.ArbiterTribunal),
      new TrainContinuously(Protoss.Arbiter, 1),
      new TrainContinuously(Protoss.DarkTemplar, 2)),
    new If(
      new UnitsAtLeast(1, Protoss.Arbiter, complete = true),
      new TrainContinuously(Protoss.Scout),
      new TrainContinuously(Protoss.Scout, 3)),
    new If(
      new And(
        new EnemyIsTerran,
        new UnitsAtMost(15, Protoss.Dragoon)),
      new Parallel(
        new Build(RequestUpgrade(Protoss.DragoonRange)),
        new TrainContinuously(Protoss.Dragoon)),
      new TrainContinuously(Protoss.Zealot)),
    new BuildOrder(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.Scout)),
    new RequireMiningBases(2),
    new BuildCannonsAtNatural(1),
    new BuildCannonsAtExpansions(2),
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(1, Protoss.ArbiterTribunal),
      RequestAtLeast(1, Protoss.Forge),
      RequestUpgrade(Protoss.ZealotSpeed)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(RequestAtLeast(10, Protoss.Gateway)),
    new FirstEightMinutes(new DefendZealotsAgainst4Pool),
    new DefendZones,
    new ScoutAt(14),
    new Attack { attackers.get.unitMatcher.set(Protoss.Scout) },
    new Trigger(
      new UnitsAtLeast(1, Protoss.Scout, complete = true),
      new ConsiderAttacking),
    new FollowBuildOrder,
    new DefendAgainstProxy,
    new RemoveMineralBlocksAt(50),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}