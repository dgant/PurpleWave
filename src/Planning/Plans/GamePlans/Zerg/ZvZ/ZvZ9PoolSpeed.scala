package Planning.Plans.GamePlans.Zerg.ZvZ

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Situational.DefendFightersAgainstEarlyPool
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpMutalisks, ScoutSafelyWithOverlord}
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Zerg.BuildSunkensInMain
import Planning.Predicates.Compound.Not
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ9PoolSpeed

class ZvZ9PoolSpeed extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ9PoolSpeed)

  override def scoutPlan: Plan = new ScoutSafelyWithOverlord
  
  override def attackPlan: Plan = new If(
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.fourPool)),
      new UnitsAtLeast(3, Zerg.Mutalisk, complete = true)),
    new Attack)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(2, Zerg.Overlord),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling))
  
  override def buildPlans: Seq[Plan] = Vector(
    new DefendFightersAgainstEarlyPool,
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new CapGasAtRatioToMinerals(1.0, 50),
      new If(
        new UnitsAtLeast(1, Zerg.Lair),
        new CapGasAt(150),
        new If(
          new GasForUpgrade(Zerg.ZerglingSpeed),
          new CapGasWorkersAt(2)))),

    new Pump(Zerg.SunkenColony),
    new If(
      new EnemyStrategy(With.fingerprints.fourPool),
      new BuildSunkensInMain(1)),

    new If(
      new GasAtLeast(100),
      new Build(
        Get(Zerg.ZerglingSpeed),
        Get(Zerg.Lair),
        Get(Zerg.Spire))),

    new Pump(Zerg.Drone, 8),
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new BuildOrder(Get(3, Zerg.Mutalisk))),
    new PumpMutalisks,
    new Pump(Zerg.Zergling),
  )
}
