package Planning.Plans.GamePlans.Zerg.ZvT

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UnitsAtMost, UpgradeStarted}
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Terran, Zerg}

class ZvT3HatchLing extends GameplanTemplate {

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(9, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(12, Zerg.Drone),
    Get(2, Zerg.Hatchery),
    Get(14, Zerg.Drone),
    Get(3, Zerg.Hatchery),
    Get(Zerg.SpawningPool),
    Get(Zerg.Extractor)
  )

  class GoSpeedlings extends Parallel(
    new Trigger(
      new Or(
        new GasAtLeast(100),
        new UpgradeStarted(Zerg.ZerglingSpeed)),
      new CapGasWorkersAt(0)),
    new Pump(Zerg.Zergling),
    new RequireMiningBases(4),
  )

  class GoLateGame extends Parallel(
    new If(
      new UnitsAtLeast(24, Zerg.Drone),
      new Parallel(
        new Build(Get(Zerg.EvolutionChamber)),
        new UpgradeContinuously(Zerg.GroundRangeDamage))),
    new If(
      new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
      new PumpRatio(Zerg.Zergling, 4, 24, Seq(Enemy(UnitMatchWarriors, 2.0)))),
    new PumpRatio(Zerg.Drone, 12, 50, Seq(Friendly(Zerg.Hatchery, 11.0))),
    new If(
      new Or(
        new EnemiesAtLeast(3, Terran.Vulture),
        new EnemiesAtLeast(2, Terran.Goliath)),
      new Build(
        Get(Zerg.HydraliskDen),
        Get(Zerg.HydraliskRange),
        Get(Zerg.HydraliskSpeed)),
      new Build(
        Get(Zerg.Lair),
        Get(Zerg.HydraliskDen),
        Get(Zerg.LurkerMorph))),
    new PumpRatio(Zerg.Extractor, 0, 4, Seq(Friendly(Zerg.Drone, 1.0 / 10.0))),
    new Pump(Zerg.Zergling)
  )

  override def buildPlans: Seq[Plan] = Seq(
    new RequireMiningBases(3),
    new Trigger(
      new UnitsAtLeast(24, Zerg.Zergling),
      new GoLateGame,
      new GoSpeedlings)
  )
}
