package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpJustEnoughScourge, PumpMutalisks, ScoutSafelyWithOverlord}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ10HatchLing

class ZvZ10HatchLing extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ10HatchLing)

  override def aggressionPlan = new Aggression(1.2)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  // https://liquipedia.net/starcraft/10_Hatch_(vs._Zerg)
  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(Get(9, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(2, Zerg.Hatchery),
      initialBefore = new ExtractorTrick),
    new BuildOrder(
      Get(10, Zerg.Drone),
      Get(2, Zerg.Hatchery),
      Get(Zerg.SpawningPool),
      Get(11, Zerg.Drone)),
    new Trigger(
      new And(
        new UnitsAtLeast(1, Zerg.SpawningPool),
        new UnitsAtLeast(9, Zerg.Drone, countEggs = true)),
      new BuildOrder(
      Get(Zerg.Extractor),
      Get(2, Zerg.Overlord),
      Get(12, Zerg.Drone),
      Get(6, Zerg.Zergling))))
  
  override def scoutPlan: Plan = new ScoutSafelyWithOverlord
  
  override def attackPlan: Plan = new Parallel(
    new Attack(Zerg.Mutalisk),
    new Hunt(Zerg.Scourge, Zerg.Mutalisk),
    new If(
      new EnemiesAtLeast(1, Zerg.Mutalisk),
      new Attack,
      new If(
        new Or(
          new EnemyStrategy(With.fingerprints.twelveHatch, With.fingerprints.twelvePool),
          new UpgradeComplete(Zerg.ZerglingSpeed),
          new UnitsAtLeast(24, Zerg.Zergling)),
        new ConsiderAttacking,
        new Scout { scouts.get.unitMatcher.set(Zerg.Zergling) })))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Not(new GasForUpgrade(Zerg.ZerglingSpeed)),
      new Parallel(
        new CapGasAt(100),
        new CapGasWorkersAt(2)),
      new If(
        new UnitsAtMost(0, Zerg.Lair),
        new Parallel(
          new CapGasAt(100),
          new CapGasWorkersAt(1)),
        new If(
          new UnitsAtMost(0, Zerg.Spire),
          new Parallel(
            new CapGasAt(150),
            new CapGasWorkersAt(2)),
          new If(
            new EnemiesAtLeast(1, Zerg.Mutalisk),
            new CapGasAtRatioToMinerals(3.0, margin = 75), // For Scourge
            new CapGasAtRatioToMinerals(1.0, margin = 75))))), // For Mutalisk

    new AllIn(
      new And(
        new UnitsAtMost(0, Zerg.Mutalisk, complete = true),
        new Or(
          new EnemiesAtLeast(1, Zerg.Spire, complete = true),
          new EnemiesAtLeast(1, Zerg.Mutalisk)))),
    
    new Pump(Zerg.Drone, 9),
    new PumpJustEnoughScourge,
    new PumpMutalisks,
    new If(
      new GasAtLeast(100),
      new Build(
        Get(Zerg.ZerglingSpeed),
        Get(Zerg.Lair),
        Get(Zerg.Spire))),
    new PumpRatio(Zerg.Zergling, 16, 200, Seq(Enemy(Zerg.Zergling, 1.2))),
    new If(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Pump(Zerg.Drone, 12)),

    new Pump(Zerg.Zergling)
  )
}
