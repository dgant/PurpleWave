package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Buildables.{RequestProduction, Get}
import Planning.Plan
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.PumpMutalisks
import Planning.Plans.GamePlans.Zerg.ZvZ.ZvZIdeas
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases}
import Planning.Plans.Placement.BuildSunkensInMain
import Planning.Predicates.Compound.{And, Latch, Not, Or}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{TechComplete, TechStarted, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy._
import Planning.UnitMatchers.MatchOr
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvR9Pool

class ZvR9Pool extends GameplanTemplate {
  
  override val activationCriteria = Employing(ZvR9Pool)

  override def scoutPlan: Plan = NoPlan()

  override def attackPlan: Plan = new If(
    new Or(
      new EnemyIsTerran,
      new EnemyIsProtoss,
      new And(
        new EnemyIsZerg,
        new Not(EnemyStrategy(With.fingerprints.fourPool))),
      UnitsAtLeast(1, Zerg.Mutalisk, complete = true),
      UnitsAtLeast(1, Zerg.Lurker, complete = true)),
    new AttackAndHarass)

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZvZIdeas.ReactToFourPool,
    new ZergReactionVsWorkerRush
  )

  class GoLurkers extends Latch(
    new And(
      UnitsAtMost(0, Zerg.Spire),
      new EnemyIsTerran))

  override def buildOrder: Seq[RequestProduction] = Vector(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(2, Zerg.Overlord),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling))

  override def buildPlans: Seq[Plan] = Vector(

    new If(
      new Or(
        new UnitsAtLeast(1, Zerg.Spire),
        new UnitsAtLeast(1, Zerg.HydraliskDen)),
      new CapGasAtRatioToMinerals(1.0, 75),
      new Parallel(
        new CapGasWorkersAt(2),
        new CapGasAt(200))),

    new Pump(Zerg.SunkenColony),
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.Lair),
        new EnemyIsProtoss),
      new Parallel(
        new BuildOrder(Get(16, Zerg.Drone)),
        new BuildSunkensInMain(4),
        new If(
          new And(
            new UnitsAtMost(3, MatchOr(Zerg.CreepColony, Zerg.SunkenColony)),
            new UnitsAtMost(0, Zerg.Spire)),
          new CapGasWorkersAt(1)))),

    new If(
      new GasAtLeast(100),
      new Parallel(
        new Build(
          Get(Zerg.Lair),
          Get(Zerg.ZerglingSpeed)),
        new If(
          new GoLurkers,
          new Build(
            Get(Zerg.HydraliskDen),
            Get(Zerg.LurkerMorph)),
          new Build(Get(Zerg.Spire))))),

    new Pump(Zerg.Drone, 10),
    new Pump(Zerg.Lurker),
    new Pump(Zerg.Hydralisk, 3),
    new If(
      new UnitsAtLeast(1, Zerg.Spire),
      new BuildOrder(Get(3, Zerg.Mutalisk))),
    new If(
      new TechStarted(Zerg.LurkerMorph),
      new BuildOrder(
        Get(2, Zerg.Hydralisk),
        Get(2, Zerg.Lurker))),
    new PumpMutalisks,

    new If(
      new Or(
        new UnitsAtLeast(1, Zerg.Spire, complete = true),
        new TechComplete(Zerg.LurkerMorph)),
      new Parallel(
        new RequireBases(2),
        new Pump(Zerg.Drone, 20),
        new BuildGasPumps(2),
        new RequireBases(3),
        new Pump(Zerg.Drone, 30),
        new BuildGasPumps(3),
        new RequireBases(4),
        new BuildGasPumps
      ),
      new Pump(Zerg.Zergling))
  )
}
