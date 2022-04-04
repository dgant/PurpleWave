package Planning.Plans.GamePlans.Zerg.ZvT

import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.{AttackAndHarass, ConsiderAttacking}
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.Or
import Planning.Predicates.Milestones.{BasesAtLeast, EnemiesAtLeast, UnitsAtLeast, UpgradeComplete}
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.{Terran, Zerg}


class ZvTLateGame extends GameplanTemplate  {

  override def attackPlan: Plan = new If(
    new BasesAtLeast(3),
    new AttackAndHarass,
    new ConsiderAttacking)

  override def buildPlans: Seq[Plan] = Seq(

    new CapGasAtRatioToMinerals(1, 400),

    new Pump(Zerg.Drone, 24),
    new RequireMiningBases(2),
    new Build(
      Get(Zerg.SpawningPool),
      Get(Zerg.Extractor),
      Get(Zerg.Lair),
      Get(Zerg.Burrow),
      Get(Zerg.OverlordSpeed),
      Get(Zerg.QueensNest),
      Get(Zerg.EvolutionChamber),
      Get(Zerg.Hive),
      Get(Zerg.UltraliskCavern),
      Get(Zerg.ZerglingAttackSpeed)),
    new RequireMiningBases(3),

    // TODO: Add a Sunken at each base vs Vultures

    new If(new UnitsAtLeast(1, Zerg.HydraliskDen), new Build(Get(Zerg.LurkerMorph))),
    new If(
      new EnemiesAtLeast(1, IsTank),
      new Parallel(
        new UpgradeContinuously(Zerg.UltraliskSpeed),
        new If(new UpgradeComplete(Zerg.UltraliskSpeed), new UpgradeContinuously(Zerg.UltraliskArmor))),
      new Parallel(
        new UpgradeContinuously(Zerg.UltraliskArmor),
        new If(new UpgradeComplete(Zerg.UltraliskArmor), new UpgradeContinuously(Zerg.UltraliskSpeed)))),
    new UpgradeContinuously(Zerg.GroundArmor),
    new If(
      new UnitsAtLeast(50, Zerg.Drone),
      new Build(Get(2, Zerg.EvolutionChamber))),
    new If(
      new Or(
        new UpgradeComplete(Zerg.GroundArmor, 3),
        new UnitsAtLeast(2, Zerg.EvolutionChamber)),
      new UpgradeContinuously(Zerg.GroundMeleeDamage)),

    new PumpRatio(Zerg.Scourge, 0, 30, Seq(Enemy(Terran.Wraith, 3), Enemy(Terran.Valkyrie, 4), Enemy(Terran.Battlecruiser, 8))),
    new If(
      new Or(
        new EnemiesAtLeast(1, IsTank),
        new EnemiesAtLeast(1, Terran.Wraith)),
      new Pump(Zerg.Mutalisk, 6)),
    new Pump(Zerg.Ultralisk),
    new Pump(Zerg.Lurker),
    new PumpRatio(Zerg.Scourge, 0, 12, Seq(Enemy(Terran.ScienceVessel, 2))),
    new PumpRatio(Zerg.Hydralisk, 0, 10, Seq(Flat(5), Friendly(Zerg.Lurker, -1), Enemy(Terran.Marine, 0.3), Enemy(Terran.Medic, 0.5), Enemy(Terran.Firebat, 0.5))),
    new PumpRatio(Zerg.Zergling, 12, 30, Seq(Enemy(Terran.Marine, 3), Enemy(Terran.Firebat, 5), Enemy(Terran.Goliath, 6), Enemy(IsTank, 8))),

    new PumpRatio(Zerg.Drone, 30, 65, Seq(Friendly(Zerg.Hatchery, 10))),
    new Pump(Zerg.Hatchery, 8, maximumConcurrently = 1),
    new Pump(Zerg.Zergling)
  )
}
