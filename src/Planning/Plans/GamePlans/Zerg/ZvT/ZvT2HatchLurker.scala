package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{MorphLurkers, UpgradeHydraRangeThenSpeed}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Cancel
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Placement.{BuildSunkensAtExpansions, BuildSunkensAtNatural}
import Planning.Plans.Scouting.ScoutNow
import Planning.Predicates.Compound.{And, Not, Or}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Predicate
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT2HatchLurker
import Utilities.Time.Seconds
import Utilities.UnitFilters._

class ZvT2HatchLurker extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT2HatchLurker)

  class EnemyMech extends And(
    new Not(new TechComplete(Zerg.LurkerMorph)),
    new Or(
      new UnitsAtLeast(1, Zerg.Spire),
      new And(
        new EnemiesAtMost(5, Terran.Marine),
        new Or(
          new EnemiesAtLeast(1, Terran.Refinery),
          EnemyWalledIn,
          new EnemyStrategy(With.fingerprints.oneFac, With.fingerprints.twoFac)))))

  override def scoutPlan: Plan = new Trigger(
    new Or(
      new MineralsForUnit(Zerg.Hatchery, 2),
      new And(
        new StartPositionsAtLeast(4),
        new MineralsForUnit(Zerg.Overlord, 2))),
    new If(
      new Not(new EnemyMech),
      new ScoutNow))

  override def attackPlan: Plan = new Parallel(
    new If(new UnitsAtLeast(1, Zerg.Mutalisk, complete = true), new AttackAndHarass),
    new If(new TechComplete(Zerg.LurkerMorph), new AttackAndHarass),
    new If(
      new EnemyMech,
      new If(
        new UpgradeComplete(Zerg.HydraliskSpeed),
        new AttackAndHarass),
      new AttackAndHarass))

  class EnoughLurkersToAttack extends UnitsAtLeast(4, Zerg.Lurker, complete = true)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder = Seq(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(2, Zerg.Hatchery),
    Get(Zerg.SpawningPool),
    Get(13, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(19, Zerg.Drone),
    Get(Zerg.Lair),
    Get(3, Zerg.Overlord))

  class CanGoMuta extends And(
    new EnemiesAtMost(0, Terran.Valkyrie),
    new EnemiesAtMost(4, Terran.Goliath))

  class HydraMuta extends Parallel(

    new CapGasAtRatioToMinerals(1.0, 100),
    new Cancel(Zerg.LurkerMorph, Zerg.ZerglingSpeed),

    new Pump(Zerg.SunkenColony),
    new BuildSunkensAtNatural(1),
    new BuildOrder(
      Get(Zerg.HydraliskDen),
      Get(3, Zerg.Hydralisk)),
    new Pump(Zerg.Drone, 16),
    new Build(Get(Zerg.Spire)),
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new Build(Get(2, Zerg.Extractor))),

    new If(
      new MiningBasesAtMost(2),
      new Parallel(
        new Pump(Zerg.Mutalisk, 12),
        new Pump(Zerg.Lurker, 1), // In case we wound up with Lurker Aspect
        new Pump(Zerg.Drone, 24),
        new Pump(Zerg.Mutalisk),
        new If(
          new Or(
            new UnitsAtLeast(9, Zerg.Mutalisk, complete = true),
            new UnitsAtLeast(22, Zerg.Drone)),
          new RequireMiningBases(4))),
      new Parallel(
        new BuildSunkensAtExpansions(1),
        new UpgradeHydraRangeThenSpeed,
        new If(
          new CanGoMuta,
          new Parallel(
            new Pump(Zerg.Drone, 18),
            new UpgradeContinuously(Zerg.AirArmor),
            new Pump(Zerg.Mutalisk),
            new Pump(Zerg.Drone)),
          new PumpRatio(Zerg.Hydralisk, 6, 30, Seq(Enemy(Terran.Goliath, 2.0)))),
        new PumpRatio(Zerg.Hydralisk, 6, 30, Seq(Enemy(Terran.Goliath, 2.0))),
        new Pump(Zerg.Drone, 30),
        new PumpRatio(Zerg.Hydralisk, 18, 30, Seq(Enemy(Terran.Vulture, 1.75), Enemy(Terran.Goliath, 2.0), Enemy(Terran.Wraith, 2.0))),
        new BuildGasPumps,
        new RequireBases(4),
        new Build(Get(Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundRangeDamage),
        new UpgradeContinuously(Zerg.OverlordSpeed),
        new Pump(Zerg.Drone, 45),
        new Pump(Zerg.Hydralisk),
        new RequireBases(8)))
  )

  class LurkerLing extends Parallel(

    new CapGasAtRatioToMinerals(2.0, 200),
    new CapGasWorkersAt(5),

    new If(
      new Not(new TechStarted(Zerg.LurkerMorph)),
      new PumpRatio(Zerg.Zergling, 4, 24, Seq(Enemy(Terran.Marine, 1.25)))),
    new BuildOrder(
      Get(Zerg.ZerglingSpeed),
      Get(Zerg.HydraliskDen)),
    new PumpRatio(Zerg.Drone, 20, 40, Seq(Friendly(IsHatchlike, 10.0))),
    new Build(Get(Zerg.LurkerMorph)),
    new BuildGasPumps,
    new If(
      new TechComplete(Zerg.LurkerMorph, Seconds(15)()),
      new MorphLurkers),
    new BuildOrder(
      Get(5, Zerg.Overlord),
      Get(5, Zerg.Hydralisk)),
    new If(
      new GasAtLeast(150),
      new Pump(Zerg.Hydralisk, maximumConcurrently = 2)),
    new Pump(Zerg.Zergling, 18),
    new RequireMiningBases(3),
    new Pump(Zerg.Zergling, 24),
    new RequireMiningBases(8)
  )

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new EnemyHasShownWraithCloak,
      new Build(Get(Zerg.OverlordSpeed))),
    new If(
      new EnemyMech,
      new HydraMuta,
      new LurkerLing)
  )
}
