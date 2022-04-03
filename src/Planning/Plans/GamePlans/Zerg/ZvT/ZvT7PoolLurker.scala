package Planning.Plans.GamePlans.Zerg.ZvT

import Macro.Buildables.{RequestProduction, Get}
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Milestones.{UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvT1HatchLurker

class ZvT7PoolLurker extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT1HatchLurker)

  override def buildOrder: Seq[RequestProduction] = Vector(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(2, Zerg.Overlord),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling))

  override def scoutPlan: Plan = NoPlan()

  override def attackPlan: Plan = new If(
    new UnitsAtLeast(1, Zerg.Lurker, complete = true),
    new AttackAndHarass,
    super.attackPlan
  )

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtMost(0, Zerg.Lair),
      new CapGasAt(100, 150, 3.0 / 9.0),
      new CapGasAt(175, 250, 3.0 / 9.0)),
    new Build(
      Get(9, Zerg.Drone),
      Get(1, Zerg.Lair),
      Get(11, Zerg.Drone),
      Get(1, Zerg.HydraliskDen)),
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen),
      new Pump(Zerg.Zergling)),
    new If(
      new UnitsAtMost(4, Zerg.Lurker),
      new Parallel(
        new Build(Get(Zerg.LurkerMorph)),
        new Pump(Zerg.Lurker),
        new Pump(Zerg.Hydralisk, 4, 2),
        new RequireMiningBases(2),
        new Pump(Zerg.Zergling),
        new Build(Get(Zerg.ZerglingSpeed))),
      new Parallel(
        new RequireMiningBases(2),
        new Build(Get(1, Zerg.Spire)),
        new Pump(Zerg.Mutalisk),
        new Pump(Zerg.Drone))),
    new BuildGasPumps,
    new Pump(Zerg.Hatchery, 5, 1)
  )
}
