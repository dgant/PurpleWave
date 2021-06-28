package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Performance.Cache
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Basic.{NoPlan, WriteStatus}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.UpgradeHydraSpeedThenRange
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Compound.{Check, Not, Or}
import Planning.Predicates.Economy.GasAtMost
import Planning.Predicates.Milestones.{EnemyHasShown, UnitsAtLeast}
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}

class ZvPLateGame extends GameplanTemplate {

  case class CompositionWeight(
    minimumArmySize: Int,
    zealot: Double,
    dragoon: Double,
    archon: Double,
    reaver: Double,
    corsair: Double) {
    def weigh: Double = if (With.units.countEnemy(MatchWarriors) < minimumArmySize) 0 else (
      zealot * With.units.countEnemy(Protoss.Zealot)
      + dragoon * With.units.countEnemy(Protoss.Dragoon)
      + archon * With.units.countEnemy(Protoss.Archon)
      + reaver * With.units.countEnemy(Protoss.Reaver)
      + corsair * With.units.countEnemy(Protoss.Corsair))
  }

  val compositionZerglingMutalisk = CompositionWeight(8,  2.0,  2.0, -6.0,  6.0, -6.0)
  val compositionZerglingDefiler  = CompositionWeight(8,  0.0,  4.0, -3.0,  3.0, -3.0)
  val compositionHydraliskLurker  = CompositionWeight(12, 3.0,  0.0,  6.0, -6.0,  3.0)
  val compositionHydraliskOnly    = CompositionWeight(0,  1.5,  1.5,  3.0, -3.0,  3.0)
  val composition = new Cache(() => Seq(
    compositionZerglingMutalisk,
    //compositionZerglingDefiler,
    compositionHydraliskLurker,
    compositionHydraliskOnly
  ).maxBy(_.weigh))

  class GoZerglingMutalisk  extends Check(() => composition() == compositionZerglingMutalisk)
  class GoZerglingDefiler   extends Check(() => composition() == compositionZerglingDefiler)
  class GoHydraliskLurker   extends Check(() => composition() == compositionHydraliskLurker)
  class GoHydraliskOnly     extends Check(() => composition() == compositionHydraliskOnly)

  override def scoutPlan: Plan = NoPlan()
  override def attackPlan: Plan = new Attack

  override def buildPlans: Seq[Plan] = Seq(
    new CapGasAt(800),

    // Basic tech
    new Pump(Zerg.Drone, 18),
    new Build(
      Get(Zerg.SpawningPool),
      Get(Zerg.Lair)),
    new If(
      new EnemyHasShown(Protoss.Corsair),
      new Parallel(
        new Build(Get(Zerg.Spire)),
        new ZvPIdeas.PumpScourgeAgainstAir)), // Critical for defense
    new RequireMiningBases(2),
    new If(new UnitsAtLeast(40, Zerg.Drone), new Build(Get(Zerg.OverlordSpeed))),
    new If(new UnitsAtLeast(40, Zerg.Drone), new If(new Not(new EnemyHasShown(Protoss.Observer)), new Build(Get(Zerg.Burrow)))),

    new If(new UnitsAtLeast(18, Zerg.Hydralisk),  new Pump(Zerg.Drone, 30)),
    new If(new UnitsAtLeast(30, Zerg.Hydralisk),  new Pump(Zerg.Drone, 50)),
    new If(new UnitsAtLeast(24, Zerg.Zergling),   new Pump(Zerg.Drone, 30)),
    new If(new UnitsAtLeast(40, Zerg.Zergling),   new Pump(Zerg.Drone, 50)),
    new If(new UnitsAtLeast(6,  Zerg.Mutalisk),   new Pump(Zerg.Drone, 30)),
    new If(new UnitsAtLeast(12, Zerg.Mutalisk),   new Pump(Zerg.Drone, 50)),
    new If(new UnitsAtLeast(2,  Zerg.Lurker),     new Pump(Zerg.Drone, 40)),
    new If(new UnitsAtLeast(4,  Zerg.Ultralisk),  new Pump(Zerg.Drone, 55)),
    new If(
      new UnitsAtLeast(1, Zerg.UltraliskCavern),
      new BuildOrder(
        Get(Zerg.UltraliskSpeed),
        Get(4, Zerg.Ultralisk),
        Get(Zerg.UltraliskArmor))),

    // Tech: Zergling + Defiler
    new If(
      new GoZerglingDefiler,
      new Parallel(
        new WriteStatus("Ling-Defiler"),
        // new Pump(Zerg.Defiler, 3),
        new PumpRatio(Zerg.Extractor, 0, 99, Seq(Friendly(Zerg.Drone, 0.1))),
        new PumpRatio(Zerg.Zergling, 24, 60, Seq(Enemy(MatchWarriors, 7.0))),
        new Build(
          Get(Zerg.QueensNest),
          Get(Zerg.EvolutionChamber),
          Get(Zerg.Hive),
          Get(Zerg.GroundArmor),
          Get(Zerg.ZerglingAttackSpeed),
          // Get(Zerg.DefilerMound),
          // Get(Zerg.Consume),
          Get(Zerg.GroundMeleeDamage),
          Get(2, Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundArmor),
        new UpgradeContinuously(Zerg.GroundMeleeDamage)
      )),

    // Tech: Zergling + Mutalisk
    new If(
      new GoZerglingMutalisk,
      new Parallel(
        new WriteStatus("Ling-Muta"),
        new Pump(Zerg.Mutalisk),
        new PumpRatio(Zerg.Extractor, 0, 99, Seq(Friendly(Zerg.Drone, 0.1))),
        new PumpRatio(Zerg.Zergling, 18, 30, Seq(Enemy(MatchWarriors, 6.0))),
        new UpgradeContinuously(Zerg.AirDamage),
        new Build(
          Get(Zerg.QueensNest),
          Get(Zerg.EvolutionChamber),
          Get(Zerg.Hive),
          Get(Zerg.GroundArmor),
          Get(Zerg.ZerglingAttackSpeed),
          Get(Zerg.GroundMeleeDamage)),
        new UpgradeContinuously(Zerg.GroundMeleeDamage))),

    // Tech: Hydralisk + Lurker
    new If(
      new GoHydraliskLurker,
      new Parallel(
        new WriteStatus("Hydra-Lurker"),
        new CapGasAt(400),
        new Pump(Zerg.Lurker),
        new Build(Get(Zerg.HydraliskDen)),
        new UpgradeHydraSpeedThenRange,
        new Build(Get(Zerg.LurkerMorph)),
        new PumpRatio(Zerg.Hydralisk, 18, 60, Seq(Enemy(MatchWarriors, 2.0))),
        new If(
          new UnitsAtLeast(50, Zerg.Drone),
          new Build(Get(2, Zerg.EvolutionChamber))),
        new UpgradeContinuously(Zerg.GroundArmor),
        new UpgradeContinuously(Zerg.GroundRangeDamage))),

    // Tech: Pure Hydralisk
    new If(
      new GoHydraliskOnly,
      new Parallel(
        new WriteStatus("Hydralisk Only"),
        new CapGasAt(250),
        new Build(Get(Zerg.HydraliskDen)),
        new UpgradeHydraSpeedThenRange,
        new PumpRatio(Zerg.Hydralisk, 18, 60, Seq(Enemy(MatchWarriors, 2.5))),
        new Build(
          Get(6, Zerg.Hatchery),
          Get(2, Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundArmor),
        new UpgradeContinuously(Zerg.GroundRangeDamage))),

    // Economy
    new Pump(Zerg.Drone, 65),
    new BuildGasPumps(2),
    new RequireMiningBases(4),
    new BuildGasPumps(3),
    new Build(Get(6, Zerg.Hatchery)),
    new RequireMiningBases(5),
    new If(new GasAtMost(400), new BuildGasPumps),
    new Pump(Zerg.Hatchery, 9, maximumConcurrently = 2),
    new BuildGasPumps,

    // Army
    new If(
      new Or(new GoZerglingDefiler, new GoZerglingMutalisk),
      new Parallel(
        new Build(
          Get(Zerg.QueensNest),
          Get(Zerg.Hive),
          Get(Zerg.UltraliskCavern)),
        new Pump(Zerg.Ultralisk),
        new Pump(Zerg.Zergling))),
    new If(
      new Or(new GoHydraliskLurker, new GoHydraliskOnly),
      new Parallel(
        new Pump(Zerg.Hydralisk),
        new Pump(Zerg.Zergling)))
  )
}
