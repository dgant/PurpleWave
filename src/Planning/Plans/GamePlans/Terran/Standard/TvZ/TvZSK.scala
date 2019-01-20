package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Terran.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyLurkers, EnemyMutalisks, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran._

class TvZSK extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvZ5Rax, TvZ2RaxTank, TvZ2RaxNuke, TvZSK)

  class CanAttack extends And(
    new Latch(new UnitsAtLeast(20, UnitMatchWarriors)),
    new Or(
      new SafeToMoveOut,
      new BasesAtLeast(3),
      new UnitsAtLeast(3, UnitMatchSiegeTank),
      new UnitsAtLeast(1, Terran.NuclearMissile)))

  class LurkerLikely extends Or(
    new EnemyHasShown(Zerg.Hydralisk),
    new EnemyHasShown(Zerg.HydraliskDen),
    new EnemyHasShown(Zerg.Lurker),
    new EnemyHasShown(Zerg.LurkerEgg))

  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new If(new CanAttack, new Attack)
  override def priorityAttackPlan: Plan = new If(new CanAttack, new Attack(UnitMatchOr(Terran.Ghost, Terran.Vulture)))
  override def workerPlan: Plan = new Parallel(
    new Pump(Terran.NuclearSilo, 1),
    new Trigger(
      new Or(
        new EnemyLurkers,
        new UnitsAtLeast(5, Terran.Barracks),
        new UnitsAtLeast(1, Terran.Factory)),
      new If(
        new Or(
          new Not(new Employing(TvZ2RaxNuke)),
          new UnitsAtLeast(1, Terran.NuclearSilo)),
        new Pump(Terran.Comsat),
        new Pump(Terran.Comsat, 1)
      )),
    new PumpWorkers(oversaturate = false))

  override def aggressionPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(5, Terran.Barracks, complete = true),
      new UnitsAtLeast(3, UnitMatchSiegeTank),
      new UnitsAtLeast(1, Terran.NuclearMissile),
      new Employing(TvZ2RaxAcademy, TvZRaxCCRax)),
    new Aggression(1.2),
    new Aggression(1.0))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(5, Terran.Marine, complete = true),
      new EjectScout),

    new RequireMiningBases(2),

    new If(
      new And(
        new Latch(new UnitsAtLeast(4, Terran.ScienceVessel, complete = true)),
        new UnitsAtLeast(30, UnitMatchWarriors)),
      new RequireMiningBases(3)),

    new TechContinuously(Terran.Stim),
    new Trigger(
      new UnitsAtLeast(1, Terran.Factory),
      new UpgradeContinuously(Terran.MarineRange)),
    new If(new Employing(TvZ2RaxNuke), new Pump(Terran.CovertOps, 1)),
    new If(new Employing(TvZ2RaxTank), new Pump(Terran.MachineShop, 1)),
    new Pump(Terran.NuclearMissile, 1),
    new If(
      new Or(
        new Not(new Employing(TvZ2RaxNuke)),
        new UnitsAtLeast(1, Terran.CovertOps))),
    new UpgradeContinuously(Terran.GhostVisionRange),

    new Trigger(
      new UnitsAtLeast(1, Terran.ScienceVessel),
      new Parallel(
        new TechContinuously(Terran.SiegeMode),
        new TechContinuously(Terran.Irradiate),
        new UpgradeContinuously(Terran.BioDamage, 1),
        new UpgradeContinuously(Terran.BioArmor, 1),
        new UpgradeContinuously(Terran.BioDamage),
        new UpgradeContinuously(Terran.BioArmor),
        new UpgradeContinuously(Terran.ScienceVesselEnergy))),

    new Pump(Terran.Ghost, 2),
    new Pump(Terran.SiegeTankUnsieged, 3),
    new Pump(Terran.ControlTower),
    new Pump(Terran.ScienceVessel, 20),
    new PumpRatio(Terran.Medic, 2, 20, Seq(Friendly(Terran.Marine, 1.0/6.0))),
    new PumpRatio(Terran.Marine, 0, 120, Seq(Enemy(Zerg.Mutalisk, 5.0))),
    new If(
      new CanAttack,
      new Parallel(
        new PumpRatio(Terran.Firebat, 0, 2, Seq(Enemy(Zerg.Zergling, 1.0))),
        new PumpRatio(Terran.Firebat, 0, 10, Seq(Enemy(Zerg.Zergling, 0.1), Enemy(Zerg.Defiler, 1.0))))),
    new Pump(Terran.Marine),
    new Pump(Terran.Vulture, 1),

    new If(
      new Employing(TvZRaxCCRax),
      new Build(Get(2, Terran.Barracks))),
    new BuildBunkersAtNatural(1),
    new If(
      new LurkerLikely,
      new Parallel(
        new BuildMissileTurretsAtNatural(1),
        new If(
          new UnitsAtMost(2, Terran.Barracks, complete = true),
          new BuildBunkersAtNatural(2)))),
    new Build(
      Get(Terran.Barracks),
      Get(Terran.Refinery),
      Get(Terran.Academy),
      Get(Terran.EngineeringBay),
      Get(Terran.BioDamage),
      Get(Terran.Stim),
      Get(2, Terran.Barracks)),

    new FlipIf(
      new EnemyLurkers,
      new If(new Employing(TvZ5Rax), new Build(Get(5, Terran.Barracks))),
      new Parallel(
        new Build(Get(Terran.Factory)),
        new If(new Employing(TvZ2RaxTank), new Build(Get(3, Terran.Barracks))),
        new BuildGasPumps,
        new If(
          new Or(
            new EnemyMutalisks,
            new Not(new EnemyLurkers)),
          new BuildMissileTurretsAtBases(3)),
        new Build(
          Get(Terran.Starport),
          Get(Terran.ScienceFacility),
          Get(2, Terran.Starport)))),

    new Trigger(
      new UnitsAtLeast(1, Terran.ScienceFacility),
      new Parallel(
        new IfOnMiningBases(2, new Build(Get(6, Terran.Barracks))),
        new IfOnMiningBases(3, new Build(Get(2, Terran.EngineeringBay), Get(3, Terran.Starport), Get(9, Terran.Barracks))),
        new IfOnMiningBases(4, new Build(Get(14, Terran.Barracks))),
        new RequireMiningBases(4))),
  )
}