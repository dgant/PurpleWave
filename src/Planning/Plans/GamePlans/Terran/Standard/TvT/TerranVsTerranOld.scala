package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.BuildRequests.Get
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic.{UpgradeContinuously, _}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import Planning.Predicates.Reactive.SafeToMoveOut
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT.TvTStandard

class TerranVsTerranOld extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvTStandard)
  
  
  
  override def defaultPlacementPlan: Plan = new BunkersAtNatural(1)
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Terran.Wraith, complete = true),
    new If(
      new SafeToMoveOut,
      new Parallel(
        new Attack,
        new Attack(UnitMatchWorkers, UnitCountOne))))
  
  override val buildOrder = Vector(
    Get(1,   Terran.CommandCenter),
    Get(9,   Terran.SCV),
    Get(1,   Terran.SupplyDepot),
    Get(11,  Terran.SCV),
    Get(1,   Terran.Barracks),
    Get(12,  Terran.SCV),
    Get(1,   Terran.Refinery),
    Get(15,  Terran.SCV),
    Get(2,   Terran.SupplyDepot),
    Get(16,  Terran.SCV),
    Get(1,   Terran.Factory),
    Get(20,  Terran.SCV))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(
      Get(1, Terran.Bunker),
      Get(1, Terran.MachineShop),
      Get(1, Terran.Starport)),
    
    new If(
      new MineralsAtLeast(800),
      new Parallel(
        new IfOnMiningBases(2, new Build(Get(5, Terran.Factory)), new Build(Get(2, Terran.Barracks))),
        new IfOnMiningBases(3, new Build(Get(8, Terran.Factory)), new Build(Get(3, Terran.Barracks)))
      )),
    
    new If(new UnitsAtLeast(1, UnitMatchSiegeTank, complete = true), new RequireMiningBases(2)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors), new RequireMiningBases(3)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors), new RequireMiningBases(4)),
    new BuildGasPumps,
    new If(new UnitsAtLeast(2,  Terran.SiegeTankUnsieged),  new Build(Get(Terran.SiegeMode))),
    new If(new UnitsAtLeast(3,  Terran.Wraith),             new Build(Get(1, Terran.ControlTower), Get(Terran.WraithCloak))),
    new If(new UnitsAtLeast(2,  Terran.Goliath),            new Build(Get(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new Build(Get(Terran.Yamato))),
    new If(new UnitsAtLeast(3,  Terran.Vulture),            new Build(Get(Terran.SpiderMinePlant))),
    new If(new UnitsAtLeast(5,  Terran.Vulture),            new Build(Get(Terran.VultureSpeed))),
    new If(new UnitsAtLeast(10, Terran.Marine),             new Build(Get(Terran.MarineRange))),
    new If(new UnitsAtLeast(4,  Terran.Goliath),            new Build(Get(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(1,  Terran.PhysicsLab),         new Pump(Terran.ControlTower)),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new UpgradeContinuously(Terran.AirDamage)),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new UpgradeContinuously(Terran.AirArmor)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechDamage)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new Build(Get(1, Terran.ScienceFacility), Get(2, Terran.Armory))),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechArmor)),
  
    new Pump(Terran.Comsat),
    new TrainMatchingRatio(Terran.Goliath, 1, Int.MaxValue, Seq(
        Enemy(Terran.Battlecruiser, 3.0),
        Enemy(Terran.Wraith,        0.75),
        Enemy(Terran.Vulture,       0.5))),
    
    new Pump(Terran.Battlecruiser),
    new TrainMatchingRatio(Terran.SiegeTankUnsieged, 3, Int.MaxValue, Seq(
      Enemy(UnitMatchSiegeTank, 1.25),
      Enemy(Terran.Goliath,     0.75),
      Enemy(Terran.Wraith,      0.75),
      Enemy(Terran.Vulture,     0.5))),
  
    new TrainMatchingRatio(Terran.Wraith, 3, Int.MaxValue, Seq(
      Enemy(Terran.Wraith,      1.5),
      Enemy(Terran.Vulture,     0.25))),
    
    new Pump(Terran.Marine),
    new Pump(Terran.Vulture),
  
    new IfOnMiningBases(2, new Build(Get(1, Terran.Starport),  Get(3, Terran.Factory), Get(2, Terran.Starport), Get(1, Terran.Armory), Get(1, Terran.Academy), Get(5, Terran.Factory))),
    new IfOnMiningBases(3, new Build(Get(5, Terran.Factory),   Get(1, Terran.Academy), Get(8, Terran.Factory))),
    new OnGasPumps(2, new Build(Get(3, Terran.MachineShop))),
    new OnGasPumps(3, new Build(
      Get(5, Terran.MachineShop),
      Get(1, Terran.ScienceFacility),
      Get(2, Terran.Starport),
      Get(1, Terran.PhysicsLab),
      Get(Terran.BattlecruiserEnergy))),
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
    new Build(Get(16, Terran.Factory))
  )
}