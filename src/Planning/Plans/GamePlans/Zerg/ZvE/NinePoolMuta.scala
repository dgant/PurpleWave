package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Economy.{GasAtMost, MineralsAtLeast}
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyIsZerg}
import Planning.UnitMatchers.UnitMatchOr
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.NinePoolMuta

class NinePoolMuta extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(NinePoolMuta)
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(9, Zerg.Drone),
    Get(1, Zerg.SpawningPool),
    Get(10, Zerg.Drone),
    Get(1, Zerg.Extractor),
    Get(2, Zerg.Overlord),
    Get(11, Zerg.Drone),
    Get(6, Zerg.Zergling))
  
  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourMain.zone),    placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)))
  
  override def scoutPlan: Plan = new If(
    new Not(new EnemiesAtLeast(1, UnitMatchOr(Zerg.Spire, Zerg.Mutalisk, Zerg.Hydralisk))),
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  override def attackPlan: Plan = new Trigger(
    new Or(
      new EnemyBasesAtLeast(2),
      new UnitsAtLeast(1, Zerg.Mutalisk),
      new Not(new EnemyIsZerg)),
    new Attack)
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtMost(0, Zerg.Lair),
      new CapGasAt(100, 100, 3.0 / 9.0),
      new If(
        new UnitsAtMost(0, Zerg.Spire),
        new CapGasAt(200, 200, 2.0 / 9.0),
        new CapGasAt(100, 300, (if (With.self.gas > With.self.minerals) 1.0 else 3.0) / 9.0))),
    new Build(
      Get(8, Zerg.Drone),
      Get(1, Zerg.Lair)),
    new Pump(Zerg.SunkenColony),
    new If(
      new UnitsAtMost(0, Zerg.Spire),
      new Parallel(
        new If(
          new Not(new EnemyIsZerg),
          new Pump(Zerg.Drone, 16)),
        new Pump(Zerg.Zergling))),
    new Build(Get(1, Zerg.Spire)),
    new If(
      new UnitsAtMost(1, Zerg.Extractor),
      new Pump(Zerg.Mutalisk, 100, 2),
      new Pump(Zerg.Mutalisk)),
    new If(
      new And(
        new Or(
          new EnemiesAtLeast(1, Terran.Vulture),
          new EnemiesAtLeast(1, Terran.Factory)),
        new UnitsAtMost(1, Zerg.SunkenColony)),
      new Build(Get(1, Zerg.CreepColony))),
    new If(
      new And(
        new UnitsAtLeast(1, Zerg.Spire),
        new UnitsAtMost(0, Zerg.SunkenColony),
        new EnemyIsZerg),
      new Build(Get(1, Zerg.CreepColony))),
    new If(
      new Or(
        new MineralsAtLeast(350),
        new Not(new EnemyIsZerg)),
      new RequireMiningBases(2)),
    new If(
      new And(
        new MineralsAtLeast(150),
        new GasAtMost(100),
        new UnitsAtLeast(2, Zerg.Larva)),
      new If(
        new And(
          new UnitsAtMost(24, Zerg.Drone),
          new Check(() =>
            With.units.countOurs(Zerg.Drone) / 9 <
            With.geography.ourBases.size)),
        new Pump(Zerg.Drone),
        new Parallel(new Pump(Zerg.Zergling)))),
    new If(
      new MineralsAtLeast(300),
      new BuildGasPumps),
    new If(
      new GasAtMost(99),
      new Pump(Zerg.Hatchery, 8, 1))
  )
}
