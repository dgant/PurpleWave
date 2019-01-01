package Planning.Plans.GamePlans.Terran.Standard.TvP

import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.{BuildBunkersAtNatural, BuildMissileTurretsAtNatural}
import Planning.Predicates.Milestones.{TechComplete, UnitsAtLeast}
import Planning.Predicates.Reactive.EnemyDarkTemplarLikely
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvPDeep4

class TvPDeep4 extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(TvPDeep4)



  override def defaultScoutExposPlan: Plan = NoPlan()

  override def defaultAttackPlan: Plan = new Parallel(
    new TvPIdeas.TvPAttack,
    new Trigger(
      new TechComplete(Terran.Stim),
      new Attack))

  override def defaultWorkerPlan: Plan = new Parallel(
    new If(
      new Or(
        new EnemyDarkTemplarLikely,
        new UnitsAtLeast(4, Terran.Barracks)),
      new Pump(Terran.Comsat)),
    new Pump(Terran.SCV, 38))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.Factory),
      Get(Terran.MachineShop)),
    new BuildBunkersAtNatural(1),
    new BuildGasPumps,
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
    new Pump(Terran.SiegeTankUnsieged),
    new Build(
      Get(4, Terran.Barracks),
      Get(Terran.BioArmor),
      Get(Terran.Stim),
      Get(Terran.MarineRange),
      Get(Terran.BioDamage)),
    new PumpMatchingRatio(Terran.Medic, 0, 12, Seq(Friendly(Terran.Marine, 0.25))),
    new Pump(Terran.Marine),
    new Build(Get(8, Terran.Barracks))
  )
}
