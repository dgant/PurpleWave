package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Aggression, ConsiderAttacking, DefendEntrance, DefendZones}
import Planning.Plans.Compound._
import Planning.Plans.Information.Employ
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost, UpgradeComplete}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{Defend2GateAgainst4Pool, DefendAgainstProxy, TwoGatewaysAtNexus}
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ.{PvZ4GateDragoonAllIn, PvZ4GateZealotAllIn}

class PvZ4GateAllIn extends Parallel {
  
  
  
  children.set(Vector(
    new RequireBareMinimum,
    new TwoGatewaysAtNexus,
    new BuildOrder(ProtossBuilds.OpeningTwoGate1012: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new If(
      new And(
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeTime(1)),
        new UnitsAtLeast(2, Protoss.Zealot),
        new Check(() => With.self.gas >= 50 )),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
    
    new Employ(PvZ4GateDragoonAllIn,
      new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(4, Protoss.Gateway)),
      new Trigger(
        new UnitsAtLeast(20, UnitMatchWarriors),
        initialAfter = new Parallel(
        new RequireMiningBases(2),
        new BuildGasPumps,
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(3, Protoss.Observer),
          RequestAtLeast(8, Protoss.Gateway)),
        new RequireMiningBases(3),
        new Build(RequestAtLeast(12, Protoss.Gateway)))))),
  
    new Employ(PvZ4GateZealotAllIn, new Build(RequestAtLeast(5, Protoss.Gateway))),
      
    new If(
      new UnitsAtMost(12, UnitMatchWarriors),
      new Aggression(1.0),
      new If(
        new UnitsAtMost(20, UnitMatchWarriors),
        new Aggression(3.0),
        new Aggression(5.0))),
  
    new FirstEightMinutes(new Defend2GateAgainst4Pool),
    new DefendZones,
    new ScoutAt(10),
    new ConsiderAttacking,
    new FollowBuildOrder,
    new DefendAgainstProxy,
    new RemoveMineralBlocksAt(30),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}

