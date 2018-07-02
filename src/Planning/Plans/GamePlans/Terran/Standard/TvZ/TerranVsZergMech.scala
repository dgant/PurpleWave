package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.TvZPlacement
import Planning.Predicates.Reactive.{EnemyLurkers, EnemyMutalisks}
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding._
import Planning.Plans.Macro.Terran.BuildMissileTurretsAtBases
import Planning.Predicates.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.TvZ.{TvZMidgameMech, TvZMidgameWraiths}

class TerranVsZergMech extends  GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Or(
    new Employing(TvZMidgameMech),
    new Employing(TvZMidgameWraiths))
  
  override def defaultPlacementPlan: Plan = new TvZPlacement
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Terran.Comsat),
    new BuildGasPumps,
    new Pump(Terran.Marine),
    new Build(Get(1, Terran.Bunker)),
    new IfOnMiningBases(2,
      new Build(
        Get(1, Terran.Barracks),
        Get(1, Terran.Refinery),
        Get(1, Terran.Factory),
        Get(1, Terran.MachineShop))),
    new If(new EnemyMutalisks, new Parallel(new BuildMissileTurretsAtBases(3), new Pump(Terran.Armory, 1), new Pump(Terran.Starport, 1), new Pump(Terran.ControlTower, 1))),
    new If(new EnemyLurkers, new Build(Get(1, Terran.EngineeringBay), Get(1, Terran.MissileTurret), Get(1, Terran.Academy))),
      
    new If(new UnitsAtLeast(2,  Terran.SiegeTankUnsieged),  new Build(Get(Terran.SiegeMode))),
    new If(new UnitsAtLeast(3,  Terran.Wraith),             new Build(Get(1, Terran.ControlTower), Get(Terran.WraithCloak))),
    new If(new UnitsAtLeast(2,  Terran.ScienceVessel),      new Build(Get(Terran.Irradiate))),
    new If(new UnitsAtLeast(2,  Terran.Goliath),            new Build(Get(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(3,  Terran.Vulture),            new Build(Get(Terran.SpiderMinePlant))),
    new If(new UnitsAtLeast(5,  Terran.Vulture),            new Build(Get(Terran.VultureSpeed))),
    new If(new UnitsAtLeast(10, Terran.Marine),             new Build(Get(Terran.MarineRange))),
    new If(new UnitsAtLeast(4,  Terran.Goliath),            new Build(Get(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechDamage)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new Build(Get(1, Terran.ScienceFacility), Get(2, Terran.Armory))),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechArmor)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new RequireMiningBases(3)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new RequireMiningBases(4)),
  
    new PumpMatchingRatio(Terran.Valkyrie, 0, 4, Seq(
      Enemy(Zerg.Mutalisk,  0.25),
      Enemy(Zerg.Guardian,  0.25))),
    
    new If(
      new Employing(TvZMidgameWraiths),
      new If(
        new UnitsAtLeast(2, Terran.ScienceVessel),
        new Pump(Terran.ScienceVessel, 2),
        new Pump(Terran.Wraith)),
      new Pump(Terran.ScienceVessel, 4)),
  
    new PumpMatchingRatio(Terran.Goliath, 1, Int.MaxValue, Seq(
      Enemy(Zerg.Mutalisk,  1.0),
      Enemy(Zerg.Guardian,  2.0))),
  
    new PumpMatchingRatio(Terran.SiegeTankUnsieged, 3, Int.MaxValue, Seq(
      Enemy(Zerg.Ultralisk,        2.0),
      Enemy(Zerg.Hydralisk,        0.4),
      Enemy(Zerg.Lurker,           0.6))),
    
    new Pump(Terran.Vulture),
  
    new If(
      new Employing(TvZMidgameWraiths),
      new IfOnMiningBases(2, new Build(
        Get(1, Terran.Factory),
        Get(2, Terran.Starport),
        Get(1, Terran.EngineeringBay),
        Get(1, Terran.Academy),
        Get(3, Terran.Barracks),
        Get(1, Terran.MachineShop),
        Get(2, Terran.Factory),
        Get(1, Terran.Armory),
        Get(6, Terran.Barracks))),
      new IfOnMiningBases(2, new Build(
        Get(1, Terran.Factory),
        Get(1, Terran.MachineShop),
        Get(1, Terran.EngineeringBay),
        Get(1, Terran.Academy),
        Get(3, Terran.Factory),
        Get(1, Terran.Starport),
        Get(1, Terran.Armory),
        Get(5, Terran.Factory)))
    ),
    new IfOnMiningBases(3, new Build(Get(5, Terran.Factory), Get(1, Terran.Academy), Get(8, Terran.Factory))),
    new OnGasPumps(2, new Build(Get(2, Terran.MachineShop))),
    new OnGasPumps(3, new Build(Get(3, Terran.MachineShop))),
    new RequireMiningBases(2),
    new Build(
      Get(3, Terran.Factory),
      Get(2, Terran.Starport),
      Get(1, Terran.Armory),
      Get(5, Terran.Factory),
      Get(1, Terran.Academy),
      Get(8, Terran.Factory)),
    new RequireMiningBases(3),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new RequireMiningBases(4),
    new Build(Get(12, Terran.Factory)),
    new RequireMiningBases(5),
    new Build(Get(16, Terran.Factory)),
    
    new RequireMiningBases(3),
    new Pump(Terran.Vulture),
    new Build(Get(16, Terran.Barracks))
  )
}