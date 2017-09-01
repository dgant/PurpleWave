package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Reactive.EnemyBasesAtLeast
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvP.PvPIdeas.{ReactToDarkTemplarExisting, ReactToDarkTemplarPossible}
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss

class PvPLateGameStandard extends Parallel {
  
  class BuildTechPartOne extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge),
      RequestUpgrade(Protoss.GroundDamage),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestTech(Protoss.PsionicStorm)))
      
  class BuildTechPartTwo extends Parallel(
    new Build(

      RequestUpgrade(Protoss.GroundArmor),
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestAtLeast(12, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestUpgrade(Protoss.ShuttleSpeed)) )
  
  children.set(Vector(
    new MeldArchons(40),
    new ReactToDarkTemplarPossible,
    new ReactToDarkTemplarExisting,
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildGasPumps,
  
    new TrainMatchingRatio(Protoss.Observer, 1, 3, Seq(MatchingRatio(Protoss.DarkTemplar, 2.0))),
    
    new If(new UnitsAtLeast(2,  Protoss.Dragoon),         new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1,  Protoss.HighTemplar),     new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2,  Protoss.Reaver),          new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(3,  Protoss.Reaver),          new If(new EnemyBasesAtLeast(3), new Build(RequestUpgrade(Protoss.ShuttleSpeed)))),
    new If(new UnitsAtLeast(8,  UnitMatchWarriors),       new RequireMiningBases(2)),
    new If(new UnitsAtLeast(15, UnitMatchWarriors),       new RequireMiningBases(3)),
    new If(new UnitsAtLeast(17, UnitMatchWarriors),       new OnMiningBases(2, new BuildTechPartOne)),
    new If(new UnitsAtLeast(25, UnitMatchWarriors),       new OnMiningBases(3, new BuildTechPartTwo)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),       new RequireMiningBases(4)),
    new If(new EnemyUnitsAtLeast(1, Protoss.DarkTemplar), new Build(RequestUpgrade(Protoss.ObserverSpeed))),
    
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new EnemyUnitsAtMost(0, Protoss.PhotonCannon)),
      new TrainContinuously(Protoss.DarkTemplar, 3)),
  
    new If(
      new And(
        new UnitsAtMost(0, Protoss.Shuttle),
        new UpgradeComplete(Protoss.ShuttleSpeed, 1, Protoss.Shuttle.buildFrames)),
      new Build(RequestAtLeast(1, Protoss.Shuttle)),
      new If(
        new And(
          new Not(new OnMiningBases(3)),
          new Not(new TechComplete(Protoss.PsionicStorm)),
          new UnitsAtMost(3, Protoss.Reaver)
        ),
        new TrainContinuously(Protoss.Reaver, 4),
        new TrainContinuously(Protoss.Observer, 3))),
  
    new If(
      new And(
        new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.ZealotSpeed.upgradeTime(1)),
        new UnitsAtMost(4, Protoss.HighTemplar),
        new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true)),
      new Build(RequestAnother(2, Protoss.HighTemplar))),
    
    new PvPIdeas.BuildDragoonsOrZealots,
    new OnMiningBases(1,
      new Build(
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(2, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(3, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new OnMiningBases(2,
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Build(RequestUpgrade(Protoss.ZealotSpeed)))),
    new OnMiningBases(2,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestAtLeast(7, Protoss.Gateway))),
    new RequireMiningBases(3),
    new OnMiningBases(1, new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
    new OnMiningBases(3, new Build(RequestAtLeast(12, Protoss.Gateway))),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.GroundDamage),
    new OnMiningBases(4, new Build(RequestAtLeast(15, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Aggression(0.82),
    new ScoutExpansionsAt(90),
    new DefendZones,
    new If(new EnemyBasesAtLeast(3), new DropAttack),
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) },
    new ConsiderAttacking
  ))
}
