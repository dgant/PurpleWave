package Planning.Plans.Protoss.GamePlans.Specialty

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army.{Aggression, ConsiderAttacking, DefendEntrance, DefendZones}
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders._
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RemoveMineralBlocksAt, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast, UnitsAtMost, UpgradeComplete}
import Planning.Plans.Protoss.ProtossBuilds
import Planning.Plans.Protoss.Situational.{Defend2GateAgainst4Pool, DefendAgainstProxy, PlaceTwoGatewaysAtNexus}
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.Protoss

class PvE4GateAllIn extends Parallel {
  
  children.set(Vector(
    new RequireEssentials,
    new PlaceTwoGatewaysAtNexus,
    new BuildOrder(ProtossBuilds.OpeningTwoGate1012: _*),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new If(
      new And(
        new Check(() => With.self.gas >= 50 ),
        new UnitsAtLeast(4, Protoss.Zealot),
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeTime(1))),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot)),
  
    new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new Build(RequestUpgrade(Protoss.DragoonRange)),
  
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(2, Protoss.Observer))),
    
    new Build(RequestAtLeast(4, Protoss.Gateway)),
    
    new Trigger(
      new UnitsAtLeast(12, UnitMatchWarriors),
      initialAfter = new Parallel(
        new RequireMiningBases(2),
        new Build(RequestAtLeast(8, Protoss.Gateway)),
        new RequireMiningBases(3),
        new Build(RequestAtLeast(12, Protoss.Gateway)))),
      
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
    new RemoveMineralBlocksAt(50),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}

