package Planning.Plans.Gameplans.Terran.TvP

import Macro.Requests.Get
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound._
import Planning.Plans.Gameplans.Terran.RepairBunker
import Planning.Plans.Gameplans.Terran.TvP.TvPIdeas.ReactiveDetection
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.PopulateBunkers
import Planning.Plans.Placement.BuildMissileTurretsAtNatural
import Planning.Predicates.Compound.{Not, Or}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemyHasShown, UnitsAtLeast}
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import Utilities.UnitFilters.{IsAny, IsTank}
import Planning.Plan
import Planning.Plans.Gameplans.All.GameplanTemplate
import Planning.Predicates.Predicate
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP6Fac

class TvP6Fac extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvP6Fac)

  override def attackPlan: Plan = new Parallel(
    new TvPIdeas.TvPAttack,
    new If(
      new Or(
        new UnitsAtLeast(6, Terran.Factory, complete = true),
        new UnitsAtLeast(8, IsTank, complete = true)),
    new AttackAndHarass))

  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers)

  override def buildPlans: Seq[Plan] = Vector(
    new RepairBunker,
    new PopulateBunkers,
    new RequireMiningBases(2),
    new TvPIdeas.PumpScienceVessels,
    new TvPIdeas.PumpGoliaths,
    new If(
      new EnemiesAtLeast(1, IsAny(Protoss.Carrier, Protoss.FleetBeacon)),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    new Pump(Terran.SiegeTankUnsieged, maximumTotal = 6, maximumConcurrently = 2),
    new ReactiveDetection,
    new If(
      new Or(
        new UnitsAtLeast(6, Terran.Factory),
        new Not(new SafeAtHome)),
      new Parallel(
        new Pump(Terran.SiegeTankUnsieged),
        new Pump(Terran.Vulture))),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(2, Terran.Factory),
      Get(Terran.MachineShop),
      Get(Terran.EngineeringBay),
      Get(Terran.SiegeMode)),
    new BuildMissileTurretsAtNatural(1),
    new If(
      new EnemyHasShown(Protoss.Shuttle),
      new Build(
        Get(Terran.Starport),
        Get(Terran.Wraith))),
    new BuildGasPumps,
    new Build(
      Get(Terran.Academy),
      Get(2, Terran.Factory),
      Get(Terran.SpiderMinePlant),
      Get(Terran.VultureSpeed),
      Get(Terran.Armory),
      Get(6, Terran.Factory),
      Get(2, Terran.MachineShop)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(Terran.Starport),
      Get(Terran.ScienceFacility),
      Get(Terran.ControlTower)),
    new Trigger(
      new UnitsAtLeast(6, Terran.Factory, complete = true),
      new Parallel(
        new RequireMiningBases(3),
        new Build(
          Get(2, Terran.Armory),
          Get(10, Terran.Factory),
          Get(4, Terran.MachineShop))))
  )
}
