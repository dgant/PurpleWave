package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.{GasAtMost, MineralsAtLeast}
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast, UnitsAtMost}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEMassGoliath

class MassGoliath extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassGoliath)
  
  override val aggression = 0.7
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(11, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(12, Terran.SCV),
    RequestAtLeast(1, Terran.Refinery),
    RequestAtLeast(13, Terran.SCV),
    RequestAtLeast(1, Terran.Marine),
    RequestAtLeast(1, Terran.Bunker),
    RequestAtLeast(14, Terran.SCV),
    RequestAtLeast(2, Terran.SupplyDepot),
    RequestAtLeast(2, Terran.Marine),
    RequestAtLeast(1, Terran.Factory))
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(35, UnitMatchWarriors),
    super.defaultAttackPlan,
    new Attack(Terran.Vulture))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new TrainContinuously(Terran.Comsat),
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Build(
          RequestAtLeast(1, Terran.Academy),
          RequestAtLeast(1, Terran.EngineeringBay),
          RequestAtLeast(1, Terran.MissileTurret)),
        new TrainContinuously(Terran.ControlTower),
        new TrainContinuously(Terran.ScienceVessel, 2))))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new UpgradeContinuously(Terran.MechDamage)),
    
    new If(
      new UnitsAtLeast(25, UnitMatchWarriors),
      new UpgradeContinuously(Terran.MechArmor)),
  
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new Parallel(
        new Build(RequestAtLeast(1, Terran.MachineShop)),
        new UpgradeContinuously(Terran.GoliathAirRange))),
  
    new If(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new Build(
        RequestAtLeast(1, Terran.Starport),
        RequestAtLeast(1, Terran.ScienceFacility),
        RequestAtLeast(2, Terran.Armory))),
    
    new If(
      new UnitsAtLeast(10, UnitMatchWarriors),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(35, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(50, UnitMatchWarriors),
      new RequireMiningBases(4)),
    new If(
      new UnitsAtLeast(70, UnitMatchWarriors),
      new RequireMiningBases(5)),
  
    new TrainContinuously(Terran.Marine, 4),
    new If(
      new Or(
        new UnitsAtMost(0, Terran.Armory, complete = true),
        new And(new MineralsAtLeast(600), new GasAtMost(50))),
      new TrainContinuously(Terran.Vulture),
      new TrainContinuously(Terran.Goliath)),
  
    new Build(RequestAtLeast(1, Terran.Armory)),
    new BuildGasPumps,
    new TrainContinuously(Terran.Factory, 30, 3),
    new RequireMiningBases(3)
  )
}