package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.FoundEnemyBase
import ProxyBwapi.Races.Zerg

class ThirteenPoolMuta extends GameplanModeTemplate {
  
  override def aggression: Double = 0.6
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new Attack {
      attackers.get.unitMatcher.set(Zerg.Zergling)
      attackers.get.unitCounter.set(UnitCountOne)
    })
  
  override def defaultAttackPlan: Plan = new Attack {
    attackers.get.unitMatcher.set(Zerg.Mutalisk)
  }
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(13, Zerg.Drone),
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(1, Zerg.Extractor),
      RequestAtLeast(14, Zerg.Drone)),
    new RequireMiningBases(2),
    new BuildOrder(
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(4, Zerg.Zergling),
      RequestAtLeast(15, Zerg.Drone),
      RequestAtLeast(1, Zerg.Spire),
      RequestAtLeast(16, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(1, Zerg.CreepColony),
      initialBefore = new Build(RequestAtLeast(1, Zerg.CreepColony))),
    new BuildOrder(
      RequestAtLeast(3, Zerg.Overlord)))
  
  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)))

  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Zerg.Mutalisk),
    new TrainContinuously(Zerg.SunkenColony),
    new TrainContinuously(Zerg.Drone, 16),
    new Build(RequestAtLeast(5, Zerg.Overlord)),
    new Trigger(
      new UnitsAtLeast(4, Zerg.Overlord, complete = true),
      new BuildGasPumps)
  )
}
