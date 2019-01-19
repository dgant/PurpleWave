package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Standard.TvP.TvPIdeas.ReactiveDetection
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Terran.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtMost
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvP2Armory

class TvP2Armory extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(TvP2Armory)

  override def defaultAttackPlan: Plan = new Parallel(
    new TvPIdeas.TvPAttack,
    new If(
      new UpgradeComplete(Terran.MechDamage, 2),
      new Attack))

  override def defaultWorkerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers(oversaturate = false))

  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(3, Terran.Marine),
      new BuildBunkersAtNatural(1)),
    new RequireMiningBases(2),
    new If(
      new UnitsAtLeast(3, Terran.ScienceVessel),
      new Build(Get(Terran.EMP))),
    new TvPIdeas.PumpScienceVessels,
    new TvPIdeas.PumpGoliaths,
    new UpgradeContinuously(Terran.GoliathAirRange),
    new Pump(Terran.SiegeTankUnsieged),
    new Pump(Terran.Vulture),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.Factory),
      Get(Terran.MachineShop),
      Get(Terran.EngineeringBay)),
    new BuildGasPumps,
    new BuildMissileTurretsAtNatural(1),
    new Build(
      Get(2, Terran.Factory),
      Get(2, Terran.MachineShop),
      Get(Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new If(
      new EnemyBasesAtMost(2),
      new Build(Get(5, Terran.Factory))), // Normally 4 but we're not very clever with our units
    new Build(
      Get(Terran.SpiderMinePlant),
      Get(Terran.VultureSpeed)),
    new ReactiveDetection,
    new BuildMissileTurretsAtBases(1),
    new RequireMiningBases(3),
    new PumpWorkers,
    new Build(
      Get(Terran.Starport),
      Get(Terran.ScienceFacility),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechArmor),
    new Pump(Terran.ControlTower),
    new Build(Get(Terran.Academy)),
    new Build(
      Get(8, Terran.Factory),
      Get(3, Terran.MachineShop)),
    new RequireBases(4),
    new Build(
      Get(12, Terran.Factory),
      Get(5, Terran.MachineShop))
  )
}