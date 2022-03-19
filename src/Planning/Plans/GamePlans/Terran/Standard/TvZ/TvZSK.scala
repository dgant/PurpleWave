package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Macro.Buildables.Get
import Planning.Plans.Army.{Aggression, AttackAndHarass, FloatBuildings}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Terran.Situational.RepairBunker
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.{BuildBunkersAtNatural, BuildMissileTurretsAtBases, BuildMissileTurretsAtNatural}
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyLurkersLikely, EnemyMutalisksLikely, SafeToMoveOut}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.{MatchOr, MatchTank, MatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Terran._

class TvZSK extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(TvZ5Rax, TvZ2RaxTank, TvZSK)

  class CanAttack extends And(
    new Or(
      new Employing(TvZ2RaxAcademy),
      new Latch(new UnitsAtLeast(20, MatchWarriors))),
    new Or(
      new SafeToMoveOut,
      new BasesAtLeast(3),
      new UnitsAtLeast(3, MatchTank),
      new UnitsAtLeast(1, Terran.NuclearMissile)))

  class LurkerLikely extends Or(
    new EnemyHasShown(Zerg.Hydralisk),
    new EnemyHasShown(Zerg.HydraliskDen),
    new EnemyHasShown(Zerg.Lurker),
    new EnemyHasShown(Zerg.LurkerEgg))

  class GoTank extends And(
    new Employing(TvZ2RaxTank),
    new Or(
      new EnemyHasTech(Zerg.LurkerMorph),
      new EnemyHasShown(Zerg.Lurker),
      new EnemyHasShown(Zerg.LurkerEgg),
      new EnemiesAtLeast(4, MatchOr(Zerg.SunkenColony, Zerg.CreepColony))))

  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new If(new CanAttack, new AttackAndHarass)
  override def workerPlan: Plan = new Parallel(
    new Trigger(
      new Or(
        new EnemyLurkersLikely,
        new UnitsAtLeast(5, Terran.Barracks),
        new UnitsAtLeast(1, Terran.Factory)),
      new Pump(Terran.Comsat)),
    new PumpWorkers(oversaturate = false))

  override def aggressionPlan: Plan = new Trigger(
    new Or(
      new UnitsAtLeast(5, Terran.Barracks, complete = true),
      new UnitsAtLeast(3, MatchTank),
      new UnitsAtLeast(1, Terran.NuclearMissile),
      new Employing(TvZ2RaxAcademy, TvZRaxCCRax)),
    new Aggression(1.2),
    new Aggression(1.0))
  
  override def buildPlans: Seq[Plan] = Vector(
    new RepairBunker,

    new If(new BasesAtMost(1), new Pump(Terran.Marine, 24, maximumConcurrently = 3)),
    new BuildBunkersAtNatural(1),
    new RequireMiningBases(2),

    new If(
      new And(
        new Latch(new UnitsAtLeast(4, Terran.ScienceVessel, complete = true)),
        new UnitsAtLeast(30, MatchWarriors)),
      new RequireMiningBases(3)),

    new TechContinuously(Terran.Stim),
    new Trigger(
      new UnitsAtLeast(1, Terran.Factory),
      new UpgradeContinuously(Terran.MarineRange)),
    new If(new GoTank, new Pump(Terran.MachineShop, 1)),
    new Pump(Terran.NuclearMissile, 1),
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
    new If(
      new And(
        new UnitsAtLeast(3, Terran.Battlecruiser),
        new EnemyHasUpgrade(Zerg.AirArmor)),
      new Parallel(
        new Build(Get(Terran.Armory)),
        new UpgradeContinuously(Terran.AirArmor))),

    new Pump(Terran.Ghost, 2),
    new If(new UnitsAtLeast(1, Terran.SiegeTankUnsieged), new TechContinuously(Terran.SiegeMode)),
    new Pump(Terran.SiegeTankUnsieged, 3),
    new Pump(Terran.ControlTower),
    new If(
      new UnitsAtMost(0, Terran.ScienceFacility, complete = true),
      new Pump(Terran.Wraith, 1)),
    new Pump(Terran.ScienceVessel, 2),
    new If(
      new TechStarted(Terran.WraithCloak),
      new PumpRatio(Terran.Wraith, 0, 12, Seq(Enemy(Zerg.Guardian, 1.0), Enemy(Zerg.Mutalisk, 1.0))),
      new PumpRatio(Terran.Wraith, 0, 12, Seq(Enemy(Zerg.Guardian, 1.0)))),
    new If(new UnitsAtLeast(5, Terran.Wraith), new Build(Get(Terran.WraithCloak))),
    new Pump(Terran.Battlecruiser),
    new Pump(Terran.ScienceVessel, 12),
    new If(
      new Or(
        new UnitsAtLeast(12, Terran.ScienceVessel),
        new EnemyHasShown(Zerg.Ultralisk)),
      new Build(Get(Terran.PhysicsLab))),
    new PumpRatio(Terran.Medic, 2, 20, Seq(Friendly(Terran.Marine, 1.0/4.0))),
    new PumpRatio(Terran.Marine, 0, 120, Seq(Enemy(Zerg.Mutalisk, 5.0))),
    new If(
      new CanAttack,
      new Parallel(
        new PumpRatio(Terran.Firebat, 0, 2, Seq(Enemy(Zerg.Zergling, 1.0))),
        new PumpRatio(Terran.Firebat, 0, 10, Seq(Enemy(Zerg.Zergling, 0.1))))),
    new If(
      new EnemyHasShown(Zerg.Defiler),
      new PumpRatio(Terran.Firebat, 0, 15, Seq(Friendly(Terran.Marine, 0.5)))),
    new Pump(Terran.Marine),

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
      new EnemyLurkersLikely,
      new If(new Not(new GoTank), new Build(Get(5, Terran.Barracks))),
      new Parallel(
        new Build(Get(Terran.Factory)),
        new If(new GoTank, new Build(Get(3, Terran.Barracks))),
        new BuildGasPumps,
        new If(
          new Or(
            new EnemyMutalisksLikely,
            new Not(new EnemyLurkersLikely)),
          new BuildMissileTurretsAtBases(3)),
        new Build(
          Get(Terran.Starport),
          Get(Terran.ScienceFacility),
          Get(2, Terran.Starport)))),

    new If(
      new And(
        new UpgradeComplete(Terran.BioArmor, 3),
        new UpgradeComplete(Terran.BioDamage, 3)),
      new FloatBuildings(Terran.EngineeringBay)),

    new Trigger(
      new UnitsAtLeast(1, Terran.ScienceFacility),
      new Parallel(
        new IfOnMiningBases(2, new Build(Get(6, Terran.Barracks))),
        new IfOnMiningBases(3, new Build(Get(2, Terran.EngineeringBay), Get(3, Terran.Starport), Get(10, Terran.Barracks))),
        new IfOnMiningBases(4, new Build(Get(14, Terran.Barracks))),
        new RequireMiningBases(4))),
  )
}