package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.{ConsiderAttacking, DefendEntrance, DefendZones}
import Planning.Plans.Compound.{And, Check, If, Parallel}
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder, FollowBuildOrder, RequireBareMinimum}
import Planning.Plans.Macro.Expanding.{RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.DefendAgainstProxy
import Planning.Plans.Recruitment.RecruitFreelancers
import ProxyBwapi.Races.Protoss

class PvZ4GateAllIn extends Parallel {
  
  children.set(Vector(
    new RequireBareMinimum,
    new BuildOrder(ProtossBuilds.OpeningTwoGate1012: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.Zealot),
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new Check(() => With.self.gas >= 50 )),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
    new Build(
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.Forge),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.DragoonRange),
      RequestAtLeast(4, Protoss.Gateway)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(12, Protoss.Gateway)),
    new DefendZones,
    new ConsiderAttacking,
    new FollowBuildOrder,
    new DefendAgainstProxy,
    new RemoveMineralBlocksAt(30),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}
