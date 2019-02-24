package Planning.Plans.GamePlans.Zerg.ZvT

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, EjectScout, Hunt}
import Planning.Plans.Basic.Do
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{MorphLurkers, UpgradeHydraRangeThenSpeed}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.CancelOrders
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Zerg.{BuildSunkensAtExpansions, BuildSunkensAtNatural}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy, StartPositionsAtLeast}
import Planning.UnitMatchers._
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT2HatchLurker

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
          new EnemyWalledIn,
          new EnemyStrategy(With.fingerprints.oneFac, With.fingerprints.twoFac)))))

  override def scoutPlan: Plan = new Trigger(
    new Or(
      new MineralsForUnit(Zerg.Hatchery, 2),
      new And(
        new StartPositionsAtLeast(4),
        new MineralsForUnit(Zerg.Overlord, 2))),
    new If(
      new Not(new EnemyMech),
      new Scout))

  override def attackPlan: Plan = new Parallel(
    new Attack(Zerg.Mutalisk),
    new If(
      new TechComplete(Zerg.LurkerMorph),
      new Attack),
    new If(
      new EnemyMech,
      new If(
        new UpgradeComplete(Zerg.HydraliskSpeed),
        new Attack),
      new Attack))

  class EnoughLurkersToAttack extends UnitsAtLeast(4, Zerg.Lurker, complete = true)

  override def aggressionPlan: Plan = new Trigger(
    new EnoughLurkersToAttack,
    new Aggression(6.0),
    new Aggression(1.0))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvTIdeas.ReactToBarracksCheese
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
    new Do(() => With.blackboard.preferCloseExpansion.set(true)),
    new CancelOrders(UnitMatchTeching(Zerg.LurkerMorph)),
    new CancelOrders(UnitMatchUpgrading(Zerg.ZerglingSpeed)),

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
        new Hunt(Zerg.Mutalisk, UnitMatchWarriors),
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

    new EjectScout,

    new CapGasAtRatioToMinerals(2.0, 200),
    new CapGasWorkersAt(5),

    new If(
      new Not(new TechStarted(Zerg.LurkerMorph)),
      new PumpRatio(Zerg.Zergling, 4, 24, Seq(Enemy(Terran.Marine, 1.25)))),
    new BuildOrder(
      Get(Zerg.ZerglingSpeed),
      Get(Zerg.HydraliskDen)),
    new PumpRatio(Zerg.Drone, 20, 40, Seq(Friendly(UnitMatchHatchery, 10.0))),
    new Build(Get(Zerg.LurkerMorph)),
    new BuildGasPumps,
    new If(
      new TechComplete(Zerg.LurkerMorph, GameTime(0, 15)()),
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
      new EnemyMech,
      new HydraMuta,
      new LurkerLing)


  )
}
