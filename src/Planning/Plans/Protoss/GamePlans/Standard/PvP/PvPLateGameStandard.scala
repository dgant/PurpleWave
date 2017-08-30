package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvP.PvPIdeas.{ReactToDarkTemplarExisting, ReactToDarkTemplarPossible}
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss

class PvPLateGameStandard extends Parallel {
  
  children.set(Vector(
    new MeldArchons(40),
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    
    new ReactToDarkTemplarPossible,
    new ReactToDarkTemplarExisting,
  
    new If(new UnitsAtLeast(2, Protoss.Dragoon),      new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(3, Protoss.Reaver),       new Build(RequestUpgrade(Protoss.ShuttleSpeed))),
    
    new TrainMatchingRatio(Protoss.Observer, 1, 2, Seq(MatchingRatio(Protoss.DarkTemplar, 2.0))),
    
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new EnemyUnitsAtMost(0, Protoss.PhotonCannon)),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new TrainContinuously(Protoss.DarkTemplar, 1)),
  
    new If(
      new And(
        new UnitsAtMost(0, Protoss.Shuttle),
        new UpgradeComplete(Protoss.ShuttleSpeed, Protoss.Shuttle.buildFrames)),
      new Build(RequestAtLeast(1, Protoss.Shuttle)),
      new TrainContinuously(Protoss.Reaver, 4)),
  
    new If(
      new And(
        new UnitsAtMost(4, Protoss.HighTemplar),
        new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true)),
      new Build(RequestAnother(1, Protoss.HighTemplar))),
    
    new PvPIdeas.BuildDragoonsOrZealots,
  
    new OnMiningBases(1,
      new Build(
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(2, Protoss.Gateway),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(3, Protoss.Gateway))),
    new RequireMiningBases(2),
    new OnMiningBases(2, new BuildGasPumps),
    new OnMiningBases(2,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Forge),
        RequestUpgrade(Protoss.GroundDamage),
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestUpgrade(Protoss.ZealotSpeed),
        RequestAtLeast(6, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new RequireMiningBases(3),
    new OnMiningBases(3, new Build(
        RequestAtLeast(2, Protoss.RoboticsFacility),
        RequestAtLeast(12, Protoss.Gateway))),
    new BuildCannonsAtExpansions(3),
    new RequireMiningBases(4),
    new OnMiningBases(4, new Build(RequestAtLeast(15, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundArmor),
  
    new Aggression(0.85),
    new ScoutExpansionsAt(90),
    new DefendZones,
    new DropAttack,
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) },
    new ConsiderAttacking
  ))
}
