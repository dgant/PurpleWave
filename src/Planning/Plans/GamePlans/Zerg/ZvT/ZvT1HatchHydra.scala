package Planning.Plans.Gameplans.Zerg.ZvT

import Lifecycle.With
import Macro.Requests.{RequestBuildable, Get}
import Planning.Plan
import Planning.Plans.Army.{AllInIf, AttackAndHarass}
import Planning.Plans.Basic.Write
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.Gameplans.All.GameplanTemplate
import Planning.Plans.Gameplans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Economy.SupplyBlocked
import Planning.Predicates.Milestones.{EnemiesAtLeast, GasForUnit, UnitsAtLeast}
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.ZvT1HatchHydra

class ZvT1HatchHydra extends GameplanTemplate {

  override val activationCriteria = new Employing(ZvT1HatchHydra)

  override def scoutPlan: Plan = new If(
    new StartPositionsAtLeast(3),
    new If(
      new StartPositionsAtLeast(4),
      new ScoutOn(Zerg.Extractor),
      new ScoutOn(Zerg.HydraliskDen)))

  override def attackPlan: Plan = new AttackAndHarass
  override def supplyPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Zerg.HydraliskDen),
    new If(
      new SupplyBlocked,
      new Pump(Zerg.Overlord, maximumConcurrently = 1)))

  def poolOn8 = Seq(
    Get(8, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(9, Zerg.Drone),
    Get(Zerg.Extractor),
    Get(10, Zerg.Drone),
    Get(2, Zerg.Overlord),
    Get(Zerg.HydraliskDen))

  def poolOn9 = Seq(
    Get(9, Zerg.Drone),
    Get(Zerg.SpawningPool),
    Get(Zerg.Extractor),
    Get(11, Zerg.Drone),
    Get(Zerg.HydraliskDen),
    Get(2, Zerg.Overlord))

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override def buildOrder: Seq[RequestBuildable] = if (With.geography.startLocations.size < 3) poolOn8 else poolOn9

  override def buildPlans = Seq(
    new Write(With.blackboard.pushKiters, () => true),
    new FloorGasWorkersAt(1),
    new AllInIf(new EnemiesAtLeast(1, Terran.Bunker)),
    new If(
      new GasForUnit(Zerg.HydraliskDen),
      new If(
        new GasForUnit(Zerg.Hydralisk),
        new CapGasWorkersAt(1),
        new CapGasWorkersAt(2)),
      new FloorGasWorkersAt(3)),
    new Pump(Zerg.Drone, 5),
    new Pump(Zerg.Hydralisk),
    new RequireBases(2)
  )
}
