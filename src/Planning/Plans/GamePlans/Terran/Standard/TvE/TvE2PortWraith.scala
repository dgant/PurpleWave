package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchSiegeTank}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Economy.GasAtMost
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvE2PortWraith

class TvE2PortWraith extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvE2PortWraith)
  override def defaultAttackPlan = new Parallel(
    new Attack(Terran.Wraith),
    new Attack(Terran.Vulture)
  )
  
  override val buildOrder = Vector(
    Get(9, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(11, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(12, Terran.SCV),
    Get(1, Terran.Refinery),
    Get(13, Terran.SCV),
    Get(1, Terran.Marine),
    Get(1, Terran.Bunker),
    Get(14, Terran.SCV),
    Get(2, Terran.SupplyDepot),
    Get(2, Terran.Marine),
    Get(1, Terran.Factory))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new Pump(Terran.Comsat),
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Build(
          Get(1, Terran.Academy),
          Get(1, Terran.EngineeringBay)),
        new Pump(Terran.MissileTurret, 2)
    )))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
    new Pump(Terran.Comsat),
    new Pump(Terran.ControlTower, 1),
    new If(
      new UnitsAtLeast(6, UnitMatchOr(UnitMatchSiegeTank, Terran.Wraith)),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(15, UnitMatchOr(UnitMatchSiegeTank, Terran.Wraith)),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(1, Terran.ControlTower),
      new Build(Get(Terran.WraithCloak))),
    new Pump(Terran.Wraith),
    new If(
      new UnitsAtLeast(4, Terran.Vulture),
      new Parallel(
        new Pump(Terran.MachineShop, 1),
        new Build(Get(Terran.SpiderMinePlant)),
        new UpgradeContinuously(Terran.VultureSpeed))),
    new Pump(Terran.Marine, 4),
    new Build(Get(2, Terran.Starport)),
    new Pump(Terran.Vulture),
    new Build(
      Get(1, Terran.Academy),
      Get(3, Terran.Factory)),
    new RequireMiningBases(2),
    new Build(
      Get(4, Terran.Starport),
      Get(3, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new Build(
      Get(1, Terran.ScienceFacility),
      Get(6, Terran.Starport),
      Get(6, Terran.Factory)),
    new RequireMiningBases(4),
    new Build(Get(12, Terran.Starport))
  )
}
