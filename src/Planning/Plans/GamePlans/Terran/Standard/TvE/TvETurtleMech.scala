package Planning.Plans.GamePlans.Terran.Standard.TvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountExactly
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
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, EnemyUnitsAtLeast, IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Predicates.{Employing, SafeAtHome, SafeToAttack}
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
      new If(
        new SafeToAttack,
        new Parallel(
          new Attack,
          new Attack {
            attackers.get.unitMatcher.set(Terran.SCV)
            attackers.get.unitCounter.set(UnitCountExactly(3))
          })
      )),
    new If(
      new UnitsAtLeast(16, UnitMatchOr(Terran.Vulture, Terran.Goliath)),
      new Attack { attackers.get.unitMatcher.set(Terran.Vulture) })
  )
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
    new EnemyHasShownCloakedThreat,
    new Parallel(
      new Build(
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.MissileTurret),
        RequestAtLeast(1, Terran.Academy),
        RequestAtLeast(2, Terran.Comsat),
        RequestAtLeast(2, Terran.MissileTurret))
    )
  ))
  
  override lazy val buildOrder: Vector[BuildRequest] =
    if (With.enemy.isProtoss)
      Vector(
        RequestAtLeast(9,   Terran.SCV),
        RequestAtLeast(1,   Terran.SupplyDepot),
        RequestAtLeast(11,  Terran.SCV),
        RequestAtLeast(1,   Terran.Barracks),
        RequestAtLeast(13,  Terran.SCV),
        RequestAtLeast(1,   Terran.Marine),
        RequestAtLeast(14,  Terran.SCV),
        RequestAtLeast(2,   Terran.Marine),
        RequestAtLeast(15,  Terran.SCV),
        RequestAtLeast(2,   Terran.SupplyDepot),
        RequestAtLeast(16,  Terran.SCV),
        RequestAtLeast(1,   Terran.Bunker),
        RequestAtLeast(17,  Terran.SCV),
        RequestAtLeast(2,   Terran.CommandCenter),
        RequestAtLeast(18,  Terran.SCV),
        RequestAtLeast(1,   Terran.Refinery),
        RequestAtLeast(3,   Terran.Marine),
        RequestAtLeast(19,  Terran.SCV),
        RequestAtLeast(4,   Terran.Marine),
        RequestAtLeast(21,  Terran.SCV),
        RequestAtLeast(1,   Terran.Factory),
        RequestAtLeast(2,   Terran.Refinery))
    else if (With.enemy.isTerran)
      Vector(
        RequestAtLeast(9,   Terran.SCV),
        RequestAtLeast(1,   Terran.SupplyDepot),
        RequestAtLeast(14,  Terran.SCV),
        RequestAtLeast(2,   Terran.CommandCenter),
        RequestAtLeast(15,  Terran.SCV),
        RequestAtLeast(1,   Terran.Barracks),
        RequestAtLeast(16,  Terran.SCV),
        RequestAtLeast(1,   Terran.Refinery),
        RequestAtLeast(2,   Terran.SupplyDepot),
        RequestAtLeast(20,  Terran.SCV),
        RequestAtLeast(1,   Terran.Marine),
        RequestAtLeast(1,   Terran.Factory),
        RequestAtLeast(21,  Terran.SCV),
        RequestAtLeast(2,   Terran.Marine),
        RequestAtLeast(1,   Terran.Bunker),
        RequestAtLeast(23,  Terran.SCV),
        RequestAtLeast(3,   Terran.Marine),
        RequestAtLeast(1,   Terran.Refinery))
    else
      Vector(
        RequestAtLeast(1,   Terran.CommandCenter),
        RequestAtLeast(9,   Terran.SCV),
        RequestAtLeast(1,   Terran.SupplyDepot),
        RequestAtLeast(10,  Terran.SCV),
        RequestAtLeast(1,   Terran.Barracks),
        RequestAtLeast(12,  Terran.SCV),
        RequestAtLeast(1,   Terran.Bunker),
        RequestAtLeast(1,   Terran.Marine),
        RequestAtLeast(14,  Terran.SCV),
        RequestAtLeast(2,   Terran.Marine),
        RequestAtLeast(15,  Terran.SCV),
        RequestAtLeast(3,   Terran.Marine),
        RequestAtLeast(2,   Terran.SupplyDepot),
        RequestAtLeast(2,   Terran.CommandCenter),
        RequestAtLeast(4,   Terran.Marine),
        RequestAtLeast(16,  Terran.SCV),
        RequestAtLeast(1,   Terran.Refinery))
  
  private class TrainArmy extends Parallel(
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new If(
          new UnitsAtLeast(2, Terran.Factory),
          new Build(RequestAtLeast(1, Terran.Starport))),
        new TrainContinuously(Terran.ScienceFacility, 1),
        new TrainContinuously(Terran.ControlTower, 2),
        new TrainContinuously(Terran.ScienceVessel, 2)
      )),
    new If(
      new Check(() => {
        val tanks = With.units.countOurs(UnitMatchSiegeTank)
        val enemyHasFlyers = With.units.enemy.exists(_.is(UnitMatchAnd(UnitMatchWarriors, UnitMatchMobileFlying)))
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
        new Build(RequestAtLeast(1, Terran.Armory)),
        new BuildGasPumps,
        new TrainContinuously(Terran.Valkyrie, 4),
        new TrainContinuously(Terran.Goliath),
        new UpgradeContinuously(Terran.GoliathAirRange),
        new TrainContinuously(Terran.Marine))),
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
    new IfOnMiningBases(1, new Build(RequestAtLeast(1, Terran.Factory))),
    new IfOnMiningBases(1, new Build(RequestAtLeast(1, Terran.MachineShop))),
    new If(new EnemyUnitsAtLeast(1, UnitMatchSiegeTank),  new Build(RequestAtLeast(1, Terran.Starport))),
    new If(new EnemyUnitsAtLeast(1, Protoss.Reaver),      new Build(RequestAtLeast(1, Terran.Starport))),
    new If(new EnemyUnitsAtLeast(1, Protoss.Shuttle),     new Build(RequestAtLeast(1, Terran.Starport))),
    new If(new EnemyIsZerg, new Build(RequestAtLeast(1, Terran.Armory))),
    new IfOnMiningBases(1, new Build(RequestAtLeast(2, Terran.Factory))),
    new If(new Not(new EnemyIsProtoss), new Build(RequestAtLeast(1, Terran.Starport))),
    new IfOnMiningBases(2, new Build(RequestAtLeast(4, Terran.Factory))),
    new IfOnMiningBases(2, new Build(RequestAtLeast(2, Terran.MachineShop))),
    new IfOnMiningBases(2, new Build(RequestAtLeast(5, Terran.Factory))),
    new IfOnMiningBases(3, new Build(RequestAtLeast(8, Terran.Factory))),
    new IfOnMiningBases(4, new Build(RequestAtLeast(12, Terran.Factory)))
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
      RequestAtLeast(1, Terran.Factory),
      RequestAtLeast(1, Terran.MachineShop),
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(2, Terran.Factory),
      RequestAtLeast(1, Terran.SiegeTankUnsieged),
      RequestTech(Terran.SiegeMode),
      RequestAtLeast(1, Terran.MissileTurret),
      RequestAtLeast(2, Terran.Refinery)),
    
    new BuildGasPumps,
  
    new If(
      new UnitsAtLeast(2, Terran.Goliath),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    
    new If(
      new UnitsAtLeast(2, Terran.Vulture),
      new UpgradeContinuously(Terran.VultureSpeed)),
  
    new If(
      new UnitsAtLeast(3, Terran.Vulture),
      new Build(RequestTech(Terran.SpiderMinePlant))),
  
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
      RequestAtLeast(1, Terran.EngineeringBay),
      RequestAtLeast(1, Terran.Armory),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(2, Terran.Comsat)),
    new UpgradeContinuously(Terran.MechDamage),
    new If(
      new UnitsAtLeast(12, UnitMatchOr(UnitMatchSiegeTank, Terran.Goliath)),
      new RequireMiningBases(3)),
    new Build(
      RequestAtLeast(6, Terran.Factory),
      RequestAtLeast(3, Terran.MachineShop),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(3, Terran.Comsat),
      RequestUpgrade(Terran.MechArmor),
      RequestAtLeast(1, Terran.Starport),
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(2, Terran.Armory),
      RequestAtLeast(8, Terran.Factory)),
    new UpgradeContinuously(Terran.MechArmor),
    new If(
      new UnitsAtLeast(20, UnitMatchOr(UnitMatchSiegeTank, Terran.Goliath)),
      new RequireMiningBases(4)),
    new Build(
      RequestAtLeast(14, Terran.Factory),
      RequestAtLeast(6, Terran.MachineShop))
  )
}