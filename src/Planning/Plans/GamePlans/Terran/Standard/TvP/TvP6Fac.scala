package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.Get
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.BuildMissileTurretsAtBases
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchSiegeTank}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvP6Fac

class TvP6Fac extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(TvP6Fac)

  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Terran.Vulture),
    new ConsiderAttacking)

  override def defaultWorkerPlan: Plan = new Parallel(
    new Pump(Terran.Comsat),
    new PumpWorkers)

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new PumpMatchingRatio(Terran.Goliath, 0, 30, Seq(
      Enemy(Protoss.Carrier,  6.0),
      Enemy(Protoss.Arbiter,  2.0),
      Enemy(Protoss.Scout,    2.0),
      Enemy(Protoss.Shuttle,  1.0),
      Friendly(UnitMatchSiegeTank, 0.2))),
    new If(
      new EnemiesAtLeast(1, UnitMatchOr(Protoss.Carrier, Protoss.FleetBeacon)),
      new UpgradeContinuously(Terran.GoliathAirRange)),
    new Pump(Terran.SiegeTankUnsieged, maximumTotal = 6, maximumConcurrently = 2),
    new If(
      new And(
        new Or(
          new MiningBasesAtLeast(3),
          new EnemyHasShown(Protoss.DarkTemplar, 1),
          new EnemyHasShown(Protoss.Arbiter),
          new EnemyHasShown(Protoss.ArbiterTribunal)),
        new UnitsAtLeast(2, Terran.Factory)),
      new Parallel(
        new Build(
          Get(Terran.Starport),
          Get(Terran.ScienceFacility)),
        new Pump(Terran.ScienceVessel, 2))),
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
    new BuildMissileTurretsAtBases(1), // Just natural
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
      Get(Terran.ScienceVessel)),
    new RequireMiningBases(3),
    new Build(
      Get(2, Terran.Armory),
      Get(10, Terran.Factory),
      Get(4, Terran.MachineShop))
  )
}
