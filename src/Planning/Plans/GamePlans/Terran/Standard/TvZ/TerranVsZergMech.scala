package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.{EnemyLurkers, EnemyMutalisks}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.TvZ.{TvZMidgameMech, TvZMidgameWraiths}

class TerranVsZergMech extends  GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Or(
    new Employing(TvZMidgameMech),
    new Employing(TvZMidgameWraiths))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.Comsat),
    new BuildGasPumps,
    new TrainContinuously(Terran.Marine),
    new Build(RequestAtLeast(1, Terran.Bunker)),
    new IfOnMiningBases(2,
      new Build(
        RequestAtLeast(1, Terran.Barracks),
        RequestAtLeast(1, Terran.Refinery),
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(1, Terran.MachineShop))),
    new If(new EnemyMutalisks, new Parallel(new BuildMissileTurretsAtBases(3), new TrainContinuously(Terran.Armory, 1), new TrainContinuously(Terran.Starport, 1), new TrainContinuously(Terran.ControlTower, 1))),
    new If(new EnemyLurkers, new Build(RequestAtLeast(1, Terran.EngineeringBay), RequestAtLeast(1, Terran.MissileTurret), RequestAtLeast(1, Terran.Academy))),
      
    new If(new UnitsAtLeast(2,  Terran.SiegeTankUnsieged),  new Build(RequestTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(3,  Terran.Wraith),             new Build(RequestAtLeast(1, Terran.ControlTower), RequestTech(Terran.WraithCloak))),
    new If(new UnitsAtLeast(2,  Terran.ScienceVessel),      new Build(RequestTech(Terran.Irradiate))),
    new If(new UnitsAtLeast(2,  Terran.Goliath),            new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(3,  Terran.Vulture),            new Build(RequestTech(Terran.SpiderMinePlant))),
    new If(new UnitsAtLeast(5,  Terran.Vulture),            new Build(RequestUpgrade(Terran.VultureSpeed))),
    new If(new UnitsAtLeast(10, Terran.Marine),             new Build(RequestUpgrade(Terran.MarineRange))),
    new If(new UnitsAtLeast(4,  Terran.Goliath),            new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechDamage)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new Build(RequestAtLeast(1, Terran.ScienceFacility), RequestAtLeast(2, Terran.Armory))),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechArmor)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new RequireMiningBases(3)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new RequireMiningBases(4)),
  
    new TrainMatchingRatio(Terran.Valkyrie, 0, 4, Seq(
      MatchingRatio(Zerg.Mutalisk,  0.25),
      MatchingRatio(Zerg.Guardian,  0.25))),
    
    new If(
      new Employing(TvZMidgameWraiths),
      new If(
        new UnitsAtLeast(2, Terran.ScienceVessel),
        new TrainContinuously(Terran.ScienceVessel, 2),
        new TrainContinuously(Terran.Wraith)),
      new TrainContinuously(Terran.ScienceVessel, 4)),
  
    new TrainMatchingRatio(Terran.Goliath, 1, Int.MaxValue, Seq(
      MatchingRatio(Zerg.Mutalisk,  1.0),
      MatchingRatio(Zerg.Guardian,  2.0))),
  
    new TrainMatchingRatio(Terran.SiegeTankUnsieged, 3, Int.MaxValue, Seq(
      MatchingRatio(Zerg.Ultralisk,        2.0),
      MatchingRatio(Zerg.Hydralisk,        0.4),
      MatchingRatio(Zerg.Lurker,           0.6))),
    
    new TrainContinuously(Terran.Vulture),
  
    new If(
      new Employing(TvZMidgameWraiths),
      new IfOnMiningBases(2, new Build(
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(2, Terran.Starport),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(3, Terran.Barracks),
        RequestAtLeast(1, Terran.MachineShop),
        RequestAtLeast(2, Terran.Factory),
        RequestAtLeast(1, Terran.Armory),
        RequestAtLeast(6, Terran.Barracks))),
      new IfOnMiningBases(2, new Build(
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(1, Terran.MachineShop),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(3, Terran.Factory),
        RequestAtLeast(1, Terran.Starport),
        RequestAtLeast(1, Terran.Armory),
        RequestAtLeast(5, Terran.Factory)))
    ),
    new IfOnMiningBases(3, new Build(RequestAtLeast(5, Terran.Factory), RequestAtLeast(1, Terran.Academy), RequestAtLeast(8, Terran.Factory))),
    new OnGasPumps(2, new Build(RequestAtLeast(2, Terran.MachineShop))),
    new OnGasPumps(3, new Build(RequestAtLeast(3, Terran.MachineShop))),
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
    new Build(RequestAtLeast(16, Terran.Barracks))
  )
}