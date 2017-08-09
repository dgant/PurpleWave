package Planning.Plans.Terran.GamePlans

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.{EnemyLurkers, EnemyMutalisks}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{OnGasBases, OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Recruitment.RecruitFreelancers
import Planning.Plans.Scouting.ScoutAt
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.TvZ.TvZMidgameWraiths

class TerranVsZergMech extends Parallel {
  
  children.set(Vector(
    new RequireSufficientSupply,
    new TrainContinuously(Terran.Comsat),
    new TrainWorkersContinuously,
    new BuildRefineries,
  
    new Build(RequestAtLeast(1, Terran.Bunker)),
    new If(new EnemyMutalisks, new BuildMissileTurretsAtBases(3)),
    new If(new EnemyLurkers, new Build(RequestAtLeast(1, Terran.EngineeringBay), RequestAtLeast(1, Terran.MissileTurret), RequestAtLeast(1, Terran.Academy))),
      
    new If(new UnitsAtLeast(2,  UnitMatchType(Terran.SiegeTankUnsieged)), new Build(RequestTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(3,  UnitMatchType(Terran.Wraith)),            new Build(RequestAtLeast(1, Terran.ControlTower), RequestTech(Terran.WraithCloak))),
    new If(new UnitsAtLeast(2,  UnitMatchType(Terran.ScienceVessel)),     new Build(RequestTech(Terran.Irradiate))),
    new If(new UnitsAtLeast(2,  UnitMatchType(Terran.Goliath)),           new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(3,  UnitMatchType(Terran.Vulture)),           new Build(RequestTech(Terran.SpiderMinePlant))),
    new If(new UnitsAtLeast(5,  UnitMatchType(Terran.Vulture)),           new Build(RequestUpgrade(Terran.VultureSpeed))),
    new If(new UnitsAtLeast(10, UnitMatchType(Terran.Marine)),            new Build(RequestUpgrade(Terran.MarineRange))),
    new If(new UnitsAtLeast(4,  UnitMatchType(Terran.Goliath)),           new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),                       new UpgradeContinuously(Terran.MechDamage)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),                       new Build(RequestAtLeast(1, Terran.ScienceFacility), RequestAtLeast(2, Terran.Armory))),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),                       new UpgradeContinuously(Terran.MechArmor)),
    new If(new UnitsAtLeast(40, UnitMatchWarriors),                       new RequireMiningBases(3)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),                       new RequireMiningBases(4)),
  
  
    new TrainMatchingRatio(Terran.Valkyrie, 0, 4, Seq(
      MatchingRatio(UnitMatchType(Zerg.Mutalisk),  0.25),
      MatchingRatio(UnitMatchType(Zerg.Guardian),  0.25))),
    new If(
      new Employing(TvZMidgameWraiths),
      new If(
        new UnitsAtLeast(2, UnitMatchType(Terran.ScienceVessel)),
        new TrainContinuously(Terran.ScienceVessel, 2),
        new TrainContinuously(Terran.Wraith)),
      new TrainContinuously(Terran.ScienceVessel, 4)),
  
    new TrainMatchingRatio(Terran.Goliath, 1, Int.MaxValue, Seq(
      MatchingRatio(UnitMatchType(Zerg.Mutalisk),  1.0),
      MatchingRatio(UnitMatchType(Zerg.Guardian),  2.0))),
  
    new TrainMatchingRatio(Terran.SiegeTankUnsieged, 3, Int.MaxValue, Seq(
      MatchingRatio(UnitMatchType(Zerg.Ultralisk),        2.0),
      MatchingRatio(UnitMatchType(Zerg.Hydralisk),        0.4),
      MatchingRatio(UnitMatchType(Zerg.Lurker),           0.6))),
  
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.Vulture),
  
    new If(
      new Employing(TvZMidgameWraiths),
      new OnMiningBases(2, new Build(
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(2, Terran.Starport),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(3, Terran.Barracks),
        RequestAtLeast(2, Terran.Factory),
        RequestAtLeast(1, Terran.Armory))),
      new OnMiningBases(2, new Build(
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(1, Terran.MachineShop),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(3, Terran.Factory),
        RequestAtLeast(1, Terran.Starport),
        RequestAtLeast(1, Terran.Armory),
        RequestAtLeast(5, Terran.Factory)))
    ),
    new OnMiningBases(3, new Build(RequestAtLeast(5, Terran.Factory), RequestAtLeast(1, Terran.Academy), RequestAtLeast(8, Terran.Factory))),
    new OnGasBases(2, new Build(RequestAtLeast(2, Terran.MachineShop))),
    new OnGasBases(3, new Build(RequestAtLeast(3, Terran.MachineShop))),
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(3, Terran.Factory),
      RequestAtLeast(2, Terran.Starport),
      RequestAtLeast(1, Terran.Armory),
      RequestAtLeast(5, Terran.Factory),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(8, Terran.Factory)),
    new RequireMiningBases(3),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(12, Terran.Factory)),
    new RequireMiningBases(5),
    new Build(RequestAtLeast(16, Terran.Factory)),
    
    new RequireMiningBases(3),
    new TrainContinuously(Terran.Vulture),
    new Build(RequestAtLeast(16, Terran.Barracks)),
    new ScoutAt(16),
    new DefendZones,
    new ConsiderAttacking,
    new FollowBuildOrder,
    new Scan,
    new RemoveMineralBlocksAt(40),
    new Gather,
    new RecruitFreelancers,
    new DefendEntrance
  ))
}