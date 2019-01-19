package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones.{BasesAtLeast, EnemyHasShown, IfOnMiningBases, UnitsAtLeast}
import Planning.Predicates.Reactive.{EnemyLurkers, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran.{TvZ2RaxNuke, TvZ3RaxTank, TvZ5Rax, TvZSK}

class TvZSK extends GameplanModeTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvZ5Rax, TvZ3RaxTank, TvZ2RaxNuke, TvZSK)

  class CanAttack extends And(
    new Latch(new UnitsAtLeast(20, UnitMatchWarriors)),
    new Or(
      new SafeToMoveOut,
      new BasesAtLeast(3)))

  class LurkerLikely extends Or(
    new EnemyHasShown(Zerg.Hydralisk),
    new EnemyHasShown(Zerg.HydraliskDen),
    new EnemyHasShown(Zerg.Lurker),
    new EnemyHasShown(Zerg.LurkerEgg))

  override def defaultScoutPlan: Plan = NoPlan()
  override def defaultAttackPlan: Plan = new If(new CanAttack, new Attack)
  override def defaultWorkerPlan: Plan = new Parallel(
    new Trigger(
      new Or(
        new EnemyLurkers,
        new UnitsAtLeast(5, Terran.Barracks)),
      new Pump(Terran.Comsat)),
    new PumpWorkers(oversaturate = false))
  
  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),

    new If(
      new And(
        new Latch(new UnitsAtLeast(4, Terran.ScienceVessel, complete = true)),
        new UnitsAtLeast(30, UnitMatchWarriors)),
      new RequireMiningBases(3)),

    new TechContinuously(Terran.Stim),
    new UpgradeContinuously(Terran.MarineRange),
    new TechContinuously(Terran.Irradiate),
    new TechContinuously(Terran.SiegeMode),
    new UpgradeContinuously(Terran.ScienceVesselEnergy),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Pump(Terran.ScienceVessel, 20),
    new PumpMatchingRatio(Terran.Medic, 2, 20, Seq(Friendly(Terran.Marine, 0.25))),
    new PumpMatchingRatio(Terran.Marine, 0, 120, Seq(Enemy(Zerg.Mutalisk, 5.0))),
    new If(
      new CanAttack,
      new PumpMatchingRatio(Terran.Firebat, 0, 2, Seq(Enemy(Zerg.Zergling, 1.0)))),
    new PumpMatchingRatio(Terran.Firebat, 0, 10, Seq(Friendly(Terran.Marine, 0.1), Enemy(Zerg.Defiler, 1.0))),
    new Pump(Terran.Marine),

    new BuildBunkersAtNatural(1),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.EngineeringBay)),
    new BuildMissileTurretsAtBases(1),
    new Build(
      Get(Terran.Academy),
      Get(Terran.BioDamage),
      Get(2, Terran.Barracks)),
    new If(
      new Employing(TvZ5Rax),
      new Build(Get(5, Terran.Barracks))),

    new FlipIf(
      new LurkerLikely,
      new BuildMissileTurretsAtBases(3),
      new Parallel(
        new BuildGasPumps,
        new If(
          new LurkerLikely,
          new BuildMissileTurretsAtNatural(2),
          new BuildBunkersAtNatural(2)),
        new Build(
          Get(Terran.Factory),
          Get(Terran.Starport),
          Get(Terran.ScienceFacility),
          Get(2, Terran.Starport)),
        new Pump(Terran.ControlTower))),

    new IfOnMiningBases(2, new Build(Get(6, Terran.Barracks))),
    new IfOnMiningBases(3, new Build(Get(2, Terran.EngineeringBay), Get(3, Terran.Starport), Get(9, Terran.Barracks))),
    new IfOnMiningBases(4, new Build(Get(14, Terran.Barracks))),
    new RequireMiningBases(4)
  )
}