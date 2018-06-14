package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{BuildRequest, GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers._
import Planning.Plan
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.{BuildBunkersAtExpansions, BuildMissileTurretsAtBases, PopulateBunkers}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtMost}
import Planning.Plans.Predicates.Matchup.{EnemyIsProtoss, EnemyIsZerg}
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, EnemiesAtLeast, IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Predicates.{Employing, SafeAtHome, SafeToMoveOut}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Terran.TvE.TvETurtleMech

class TvETurtleMech extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvETurtleMech)
  
  override def defaultPlacementPlan: Plan = new ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = Vector(
      new Blueprint(this, building = Some(Terran.Bunker),         requireZone = Some(With.geography.ourNatural.zone)),
      new Blueprint(this, building = Some(Terran.Barracks),       preferZone  = Some(With.geography.ourNatural.zone)),
      new Blueprint(this, building = Some(Terran.SupplyDepot),    preferZone  = Some(With.geography.ourNatural.zone)),
      new Blueprint(this, building = Some(Terran.SupplyDepot),    preferZone  = Some(With.geography.ourNatural.zone)),
      new Blueprint(this, building = Some(Terran.MissileTurret),  requireZone = Some(With.geography.ourNatural.zone)),
      new Blueprint(this, building = Some(Terran.MissileTurret),  preferZone  = Some(With.geography.ourNatural.zone))
    )
  }
  
  override def priorityAttackPlan: Plan = new PopulateBunkers
  
  override def defaultAttackPlan: Plan = new Parallel(
    new If(
      new UnitsAtLeast(5, UnitMatchSiegeTank, complete = true),
      new If(new SafeToMoveOut, new Attack)),
    new If(
      new UnitsAtLeast(4, UnitMatchOr(Terran.Vulture, Terran.Goliath)),
      new Attack(Terran.Vulture))
  )
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
    new EnemyHasShownCloakedThreat,
    new Parallel(
      new Build(
        GetAtLeast(1, Terran.EngineeringBay),
        GetAtLeast(1, Terran.MissileTurret),
        GetAtLeast(1, Terran.Academy),
        GetAtLeast(2, Terran.Comsat),
        GetAtLeast(2, Terran.MissileTurret))
    )
  ))
  
  override lazy val buildOrder: Vector[BuildRequest] =
    if (With.enemy.isProtoss)
      Vector(
        GetAtLeast(9,   Terran.SCV),
        GetAtLeast(1,   Terran.SupplyDepot),
        GetAtLeast(11,  Terran.SCV),
        GetAtLeast(1,   Terran.Barracks),
        GetAtLeast(13,  Terran.SCV),
        GetAtLeast(1,   Terran.Marine),
        GetAtLeast(14,  Terran.SCV),
        GetAtLeast(2,   Terran.Marine),
        GetAtLeast(15,  Terran.SCV),
        GetAtLeast(2,   Terran.SupplyDepot),
        GetAtLeast(16,  Terran.SCV),
        GetAtLeast(1,   Terran.Bunker),
        GetAtLeast(17,  Terran.SCV),
        GetAtLeast(2,   Terran.CommandCenter),
        GetAtLeast(18,  Terran.SCV),
        GetAtLeast(1,   Terran.Refinery),
        GetAtLeast(3,   Terran.Marine),
        GetAtLeast(19,  Terran.SCV),
        GetAtLeast(4,   Terran.Marine),
        GetAtLeast(21,  Terran.SCV),
        GetAtLeast(1,   Terran.Factory),
        GetAtLeast(2,   Terran.Refinery))
    else if (With.enemy.isTerran)
      Vector(
        GetAtLeast(9,   Terran.SCV),
        GetAtLeast(1,   Terran.SupplyDepot),
        GetAtLeast(14,  Terran.SCV),
        GetAtLeast(2,   Terran.CommandCenter),
        GetAtLeast(15,  Terran.SCV),
        GetAtLeast(1,   Terran.Barracks),
        GetAtLeast(16,  Terran.SCV),
        GetAtLeast(1,   Terran.Refinery),
        GetAtLeast(2,   Terran.SupplyDepot),
        GetAtLeast(20,  Terran.SCV),
        GetAtLeast(1,   Terran.Marine),
        GetAtLeast(1,   Terran.Factory),
        GetAtLeast(21,  Terran.SCV),
        GetAtLeast(2,   Terran.Marine),
        GetAtLeast(1,   Terran.Bunker),
        GetAtLeast(23,  Terran.SCV),
        GetAtLeast(3,   Terran.Marine),
        GetAtLeast(1,   Terran.Refinery))
    else
      Vector(
        GetAtLeast(1,   Terran.CommandCenter),
        GetAtLeast(9,   Terran.SCV),
        GetAtLeast(1,   Terran.SupplyDepot),
        GetAtLeast(10,  Terran.SCV),
        GetAtLeast(1,   Terran.Barracks),
        GetAtLeast(12,  Terran.SCV),
        GetAtLeast(1,   Terran.Bunker),
        GetAtLeast(1,   Terran.Marine),
        GetAtLeast(14,  Terran.SCV),
        GetAtLeast(2,   Terran.Marine),
        GetAtLeast(15,  Terran.SCV),
        GetAtLeast(3,   Terran.Marine),
        GetAtLeast(2,   Terran.SupplyDepot),
        GetAtLeast(2,   Terran.CommandCenter),
        GetAtLeast(4,   Terran.Marine),
        GetAtLeast(16,  Terran.SCV),
        GetAtLeast(1,   Terran.Refinery))
  
  private class EnemyFlyers extends Check(() => With.units.existsEnemy(UnitMatchAnd(UnitMatchWarriors, UnitMatchMobileFlying)))
  private class TrainArmy extends Parallel(
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new If(
          new UnitsAtLeast(2, Terran.Factory),
          new Build(GetAtLeast(1, Terran.Starport))),
        new TrainContinuously(Terran.ScienceFacility, 1),
        new TrainContinuously(Terran.ControlTower, 2),
        new TrainContinuously(Terran.ScienceVessel, 2)
      )),
    new If(
      new Check(() => {
        val tanks = With.units.countOurs(UnitMatchSiegeTank)
        val enemyHasFlyers = new EnemyFlyers().isComplete
        val enemyFlyerStrength = With.units.enemy.map(u =>
          if (u.is(Terran.Battlecruiser)) 6 else
          if (u.is(Terran.Wraith))        2 else
          if (u.is(Protoss.Carrier))      7 else
          if (u.is(Protoss.Interceptor))  0 else
          if (u.is(Protoss.Scout))        3 else
          if (u.is(Zerg.Guardian))        4 else
          if (u.is(Zerg.Mutalisk))        3 else
          0).sum
        val goliathsNeeded = tanks/6 + (if (enemyHasFlyers) 6 else 0) + enemyFlyerStrength
        val goliathsNow = With.units.countOurs(Terran.Goliath)
        val output = goliathsNeeded > goliathsNow
        output
      }),
      new Parallel(
        new Build(GetAtLeast(1, Terran.Armory)),
        new BuildGasPumps,
        new If (new EnemyFlyers, new TrainContinuously(Terran.Valkyrie, 4)),
        new TrainContinuously(Terran.Goliath),
        new If (new EnemyFlyers, new UpgradeContinuously(Terran.GoliathAirRange)),
        new If (new EnemyFlyers, new TrainContinuously(Terran.Marine)))),
    new If(
      new Or(
        new MineralsAtMost(400),
        new GasAtLeast(200)),
      new Parallel(
        new TrainContinuously(Terran.Wraith, 2),
        new TrainContinuously(Terran.SiegeTankUnsieged))),
    new If(
      new And(
        new EnemyIsZerg,
        new UnitsAtLeast(1, Terran.Armory, complete = true)),
      new TrainContinuously(Terran.Goliath),
      new TrainContinuously(Terran.Vulture))
  )
  
  private class BuildProduction extends Parallel(
    new IfOnMiningBases(1, new Build(GetAtLeast(1, Terran.Factory))),
    new IfOnMiningBases(1, new Build(GetAtLeast(1, Terran.MachineShop))),
    new If(new EnemiesAtLeast(1, UnitMatchSiegeTank),  new Build(GetAtLeast(1, Terran.Starport))),
    new If(new EnemiesAtLeast(1, Protoss.Reaver),      new Build(GetAtLeast(1, Terran.Starport))),
    new If(new EnemiesAtLeast(1, Protoss.Shuttle),     new Build(GetAtLeast(1, Terran.Starport))),
    new If(new EnemyIsZerg, new Build(GetAtLeast(1, Terran.Armory))),
    new IfOnMiningBases(1, new Build(GetAtLeast(2, Terran.Factory))),
    new If(new Not(new EnemyIsProtoss), new Build(GetAtLeast(1, Terran.Starport))),
    new IfOnMiningBases(2, new Build(GetAtLeast(4, Terran.Factory))),
    new IfOnMiningBases(2, new Build(GetAtLeast(2, Terran.MachineShop))),
    new IfOnMiningBases(2, new Build(GetAtLeast(5, Terran.Factory))),
    new IfOnMiningBases(3, new Build(GetAtLeast(8, Terran.Factory))),
    new IfOnMiningBases(4, new Build(GetAtLeast(12, Terran.Factory)))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new Aggression(0.8),
    new If(new UnitsAtLeast(10, UnitMatchSiegeTank), new Aggression(1.5)),
    new If(new UnitsAtLeast(15, UnitMatchSiegeTank), new Aggression(2.0)),
    new If(new UnitsAtLeast(20, UnitMatchSiegeTank), new Aggression(3.0)),
    
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine)
        < 4 * With.units.countOurs(Terran.Bunker)),
      new TrainContinuously(Terran.Marine)),
    
    new BuildOrder(
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.MachineShop),
      GetAtLeast(1, Terran.EngineeringBay),
      GetAtLeast(2, Terran.Factory),
      GetAtLeast(1, Terran.SiegeTankUnsieged),
      GetTech(Terran.SiegeMode),
      GetAtLeast(1, Terran.MissileTurret),
      GetAtLeast(2, Terran.Refinery)),
    
    new BuildGasPumps,
  
    new If(
      new UnitsAtLeast(2, Terran.Goliath),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    
    new If(
      new UnitsAtLeast(2, Terran.Vulture),
      new UpgradeContinuously(Terran.VultureSpeed)),
  
    new If(
      new UnitsAtLeast(3, Terran.Vulture),
      new Build(GetTech(Terran.SpiderMinePlant))),
  
    new RequireMiningBases(2),
    
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(10, UnitMatchWarriors)),
      new TrainArmy,
      new BuildProduction),
  
    new BuildMissileTurretsAtBases(1),
    new BuildBunkersAtExpansions,
    new Build(
      GetAtLeast(1, Terran.EngineeringBay),
      GetAtLeast(1, Terran.Armory),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(2, Terran.Comsat)),
    new UpgradeContinuously(Terran.MechDamage),
    new If(
      new UnitsAtLeast(12, UnitMatchOr(UnitMatchSiegeTank, Terran.Goliath)),
      new RequireMiningBases(3)),
    new Build(
      GetAtLeast(6, Terran.Factory),
      GetAtLeast(3, Terran.MachineShop),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(3, Terran.Comsat),
      GetUpgrade(Terran.MechArmor),
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(1, Terran.ScienceFacility),
      GetAtLeast(2, Terran.Armory),
      GetAtLeast(8, Terran.Factory)),
    new UpgradeContinuously(Terran.MechArmor),
    new If(
      new UnitsAtLeast(20, UnitMatchOr(UnitMatchSiegeTank, Terran.Goliath)),
      new RequireMiningBases(4)),
    new Build(
      GetAtLeast(14, Terran.Factory),
      GetAtLeast(6, Terran.MachineShop))
  )
}