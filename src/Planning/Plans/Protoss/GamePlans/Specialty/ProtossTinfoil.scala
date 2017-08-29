package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Aggression, ConsiderAttacking, DefendEntrance, DropAttack}
import Planning.Plans.Compound.{And, Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtBases, BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.Situational.DefendProxy
import Planning.Plans.Recruitment.RecruitFreelancers
import ProxyBwapi.Races.Protoss

class ProtossTinfoil extends Parallel {
  
  // An objectively weak but very safe build that should put up a fight against anything.
  
  children.set(Vector(
    new Aggression(0.7),
    new RequireBareMinimum,
    new Build(
      RequestAtLeast(8, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(9, Protoss.Probe),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(10, Protoss.Probe),
      RequestAtLeast(2, Protoss.PhotonCannon)),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new TrainContinuously(Protoss.Observer, 2),
    new TrainContinuously(Protoss.Carrier, 2),
    new If(
      new And(
        new Check(() => With.self.gas >= 50),
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Assimilator)),
      new TrainContinuously(Protoss.Dragoon, 30),
      new TrainContinuously(Protoss.Zealot, 15)),
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.Shuttle)
    ),
    new BuildCannonsAtBases(1),
    new RequireMiningBases(2),
    new BuildGasPumps,
    new Build(RequestAtLeast(7, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(3, Protoss.Stargate),
      RequestUpgrade(Protoss.CarrierCapacity)),
    new UpgradeContinuously(Protoss.AirDamage),
    new Build(RequestAtLeast(2, Protoss.CyberneticsCore)),
    new UpgradeContinuously(Protoss.AirArmor),
    new Build(RequestAtLeast(5, Protoss.Stargate)),
    new RequireMiningBases(4),
    new BuildCannonsAtBases(6),
    new RequireMiningBases(6),
    new DropAttack { paratrooperMatcher.set(UnitMatchWarriors) },
    new ConsiderAttacking,
    new FollowBuildOrder,
    new DefendProxy,
    new RemoveMineralBlocksAt(80),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
  
}
