package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobileFlying, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvEMassVulture

class MassVulture extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvEMassVulture)
  
  override def defaultAttackPlan: Plan =
    new Trigger(
      new UnitsAtLeast(1, Terran.Vulture, complete = true),
      new Attack)
  
  override val buildOrder = Vector(
    RequestAtLeast(1,   Terran.CommandCenter),
    RequestAtLeast(9,   Terran.SCV),
    RequestAtLeast(1,   Terran.SupplyDepot),
    RequestAtLeast(11,  Terran.SCV),
    RequestAtLeast(1,   Terran.Barracks),
    RequestAtLeast(1,   Terran.Refinery),
    RequestAtLeast(13,  Terran.SCV),
    RequestAtLeast(1,   Terran.Marine),
    RequestAtLeast(15,  Terran.SCV),
    RequestAtLeast(2,   Terran.SupplyDepot),
    RequestAtLeast(1,   Terran.Factory))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(RequestAtLeast(1, Terran.Factory)),
    new Trigger(
      new UnitsAtLeast(1, Terran.Vulture),
      new TrainContinuously(Terran.MachineShop, 2)),
    new UpgradeContinuously(Terran.VultureSpeed),
    new Trigger(
      new UnitsAtLeast(2, Terran.MachineShop),
      new Build(RequestTech(Terran.SpiderMinePlant))),
    new If(
      new EnemyUnitsAtLeast(1, UnitMatchAnd(UnitMatchWarriors, UnitMatchMobileFlying)),
      new Parallel(
        new Build(RequestAtLeast(1, Terran.Armory)),
        new BuildGasPumps,
        new TrainContinuously(Terran.Goliath),
        new UpgradeContinuously(Terran.GoliathAirRange),
        new TrainContinuously(Terran.Marine)),
      new TrainContinuously(Terran.Vulture)),
    new Build(RequestAtLeast(5, Terran.Factory)),
    new RequireMiningBases(2),
    new Build(RequestAtLeast(10, Terran.Factory)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(15, Terran.Factory))
  )
}