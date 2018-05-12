package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.{UnitMatchOr, UnitMatchSiegeTank}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
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
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new TrainContinuously(Terran.Comsat),
    new If(
      new EnemyHasShownCloakedThreat,
      new Parallel(
        new Build(
          RequestAtLeast(1, Terran.Academy),
          RequestAtLeast(1, Terran.EngineeringBay)),
        new TrainContinuously(Terran.MissileTurret, 2)
    )))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new GasAtMost(300),
      new BuildGasPumps),
    new TrainContinuously(Terran.Comsat),
    new TrainContinuously(Terran.ControlTower, 1),
    new If(
      new UnitsAtLeast(6, UnitMatchOr(UnitMatchSiegeTank, Terran.Wraith)),
      new RequireMiningBases(2)),
    new If(
      new UnitsAtLeast(15, UnitMatchOr(UnitMatchSiegeTank, Terran.Wraith)),
      new RequireMiningBases(3)),
    new If(
      new UnitsAtLeast(1, Terran.ControlTower),
      new Build(RequestTech(Terran.WraithCloak))),
    new TrainContinuously(Terran.Wraith),
    new If(
      new UnitsAtLeast(4, Terran.Vulture),
      new Parallel(
        new TrainContinuously(Terran.MachineShop, 1),
        new Build(RequestTech(Terran.SpiderMinePlant)),
        new UpgradeContinuously(Terran.VultureSpeed))),
    new TrainContinuously(Terran.Marine, 4),
    new Build(RequestAtLeast(2, Terran.Starport)),
    new TrainContinuously(Terran.Vulture),
    new Build(
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(3, Terran.Factory)),
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(4, Terran.Starport),
      RequestAtLeast(3, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new Build(
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(6, Terran.Starport),
      RequestAtLeast(6, Terran.Factory)),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(12, Terran.Starport))
  )
}
