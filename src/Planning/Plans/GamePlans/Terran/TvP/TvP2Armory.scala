package Planning.Plans.GamePlans.Terran.TvP

import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.Terran.RepairBunker
import Planning.Plans.GamePlans.Terran.TvP.TvPIdeas.ReactiveDetection
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Plans.Placement.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtMost
import Planning.Predicates.Strategy.Employing
import Planning.Plan
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP2Armory

class TvP2Armory extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvP2Armory)

  override def attackPlan: Plan = new Parallel(
    new TvPIdeas.TvPAttack,
    new If(
      new UpgradeComplete(Terran.MechDamage, 2),
      new AttackAndHarass))

  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers(oversaturate = false))

  override def buildPlans: Seq[Plan] = Vector(
    new RepairBunker,
    new PopulateBunkers,
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
      Get(Terran.Factory)),
    new BuildGasPumps,
    new Build(
      Get(Terran.MachineShop),
      Get(Terran.EngineeringBay)),
    new BuildMissileTurretsAtNatural(1),
    new If(
      new EnemyHasShown(Protoss.Shuttle),
      new Build(
        Get(Terran.Starport),
        Get(Terran.Wraith))),
    new Build(
      Get(2, Terran.Factory),
      Get(2, Terran.MachineShop),
      Get(Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new If(
      new EnemyBasesAtMost(2),
      new Build(Get(4, Terran.Factory))), // Normally 4 but we're not very clever with our units
    new Build(
      Get(Terran.SpiderMinePlant),
      Get(Terran.VultureSpeed)),
    new ReactiveDetection,
    new BuildMissileTurretsAtBases(1),
    new RequireBases(3),
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
