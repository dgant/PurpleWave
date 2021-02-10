package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Situational.RepairBunker
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemyHasShown, UnitsAtLeast}
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.{MatchOr, MatchSiegeTank}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvPDeep4

class TvPDeep4 extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(TvPDeep4)

  override def attackPlan: Plan = new Parallel(
    new TvPIdeas.TvPAttack,
    new Trigger(
      new UnitsAtLeast(12, MatchOr(Terran.Marine, Terran.Medic), complete = true),
      new Parallel(
        new Aggression(1.5),
        new Attack)))

  override def workerPlan: Plan = new Parallel(
    new If(
      new Or(
        new EnemyDarkTemplarLikely,
        new UnitsAtLeast(4, Terran.Barracks)),
      new Pump(Terran.Comsat)),
    new Pump(Terran.SCV, 38))

  class ReactToCarriers extends Or(
    new EnemyHasShown(Protoss.Carrier),
    new EnemyHasShown(Protoss.Interceptor))

  override def buildPlans: Seq[Plan] = Vector(
    new RepairBunker,
    new RequireMiningBases(2),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.Factory),
      Get(Terran.MachineShop)),
    new BuildBunkersAtNatural(1),
    new BuildGasPumps,
    new If(
      new And(
        new UnitsAtLeast(5, MatchSiegeTank),
        new EnemiesAtLeast(1, MatchOr(Protoss.Arbiter, Protoss.DarkTemplar))),
      new Build(
        Get(Terran.Starport),
        Get(Terran.ScienceFacility),
        Get(Terran.ControlTower),
        Get(Terran.ScienceVessel))),
    new Pump(Terran.SiegeTankUnsieged, 2),
    new Build(Get(Terran.SiegeMode)),
    new FlipIf(
      new EnemyDarkTemplarLikely,
      new Build(
        Get(2, Terran.Factory),
        Get(2, Terran.MachineShop)),
      new Parallel(
        new Build(Get(Terran.EngineeringBay)),
        new BuildMissileTurretsAtNatural(1),
        new Build(Get(Terran.Academy)))),
    new PumpRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.25))),
    new If(
      new ReactToCarriers,
      new Parallel(
        new Build(
          Get(Terran.Armory),
          Get(Terran.GoliathAirRange),
          Get(Terran.MechDamage)),
        new Pump(Terran.SiegeTankUnsieged, 3),
        new Pump(Terran.Goliath)),
      new Pump(Terran.SiegeTankUnsieged)),
    new Build(
      Get(4, Terran.Barracks),
      Get(Terran.BioArmor),
      Get(Terran.Stim)),
    new If(
      new EnemyHasShown(Protoss.Shuttle),
      new Build(
        Get(Terran.Starport),
        Get(Terran.Wraith))),
    new Pump(Terran.Marine),
    new BuildMissileTurretsAtBases(2),
    new Build(
      Get(Terran.MarineRange),
      Get(Terran.BioDamage)),
    new If(
      new ReactToCarriers,
      new Parallel(
        new RequireMiningBases(3),
        new Build(
          Get(Terran.Starport),
          Get(Terran.ControlTower),
          Get(Terran.WraithCloak)),
        new PumpRatio(Terran.Wraith, 0, 12, Seq(Enemy(Protoss.Carrier, 3.0))))),
    new Build(Get(8, Terran.Barracks))
  )
}
