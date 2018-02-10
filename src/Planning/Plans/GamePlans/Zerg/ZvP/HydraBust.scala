package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class HydraBust extends GameplanModeTemplate {
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(12, Zerg.Drone),
    RequestAtLeast(2, Zerg.Hatchery),
    RequestAtLeast(1, Zerg.SpawningPool))
  
  override def defaultScoutPlan = new Trigger(
    new Check(() => With.self.minerals >= 300),
    new Scout)
  
  private class AddSunkens extends Parallel(
    new TrainContinuously(Zerg.SunkenColony),
    new If(
      new UnitsAtMost(1, Zerg.SunkenColony),
      new TrainContinuously(Zerg.CreepColony, 2)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new And(
        new UnitsAtMost(0, Zerg.HydraliskDen, complete = true),
        new Check(() => With.geography.ourBases.exists(_.units.exists(u => u.isEnemy && u.is(Protoss.Zealot))))),
      new Parallel(
        new TrainContinuously(Zerg.Zergling),
        new AddSunkens)),
    new If(
      new EnemyUnitsAtLeast(1, Terran.Vulture),
      new AddSunkens),
    new BuildOrder(
      RequestAtLeast(15, Zerg.Drone),
      RequestAtLeast(3, Zerg.Hatchery),
      RequestAtLeast(4, Zerg.Zergling),
      RequestAtLeast(1, Zerg.Extractor),
      RequestAtLeast(3, Zerg.Overlord),
      RequestAtLeast(16, Zerg.Drone),
      RequestAtLeast(1, Zerg.HydraliskDen),
      RequestAtLeast(20, Zerg.Drone),
      RequestUpgrade(Zerg.HydraliskSpeed),
      RequestAtLeast(22, Zerg.Drone),
      RequestAtLeast(6, Zerg.Hydralisk),
      RequestAtLeast(25, Zerg.Drone),
      RequestUpgrade(Zerg.HydraliskRange)),
    new If(
      new Check(() =>
        With.self.gas >= 25
        || With.self.minerals < 300
        || With.units.ours.count(_.is(Zerg.Larva)) < 5),
      new TrainContinuously(Zerg.Hydralisk),
      new TrainContinuously(Zerg.Zergling)),
    new If(
      new UnitsAtMost(4, Zerg.Larva),
      new RequireMiningBases(5))
  )
}
