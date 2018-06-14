package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.GetAtLeast
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
    new Attack(Zerg.Zergling, UnitCountOne))
  
  override def defaultAttackPlan: Plan = new Attack(Zerg.Mutalisk)
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      GetAtLeast(9, Zerg.Drone),
      GetAtLeast(2, Zerg.Overlord),
      GetAtLeast(13, Zerg.Drone),
      GetAtLeast(1, Zerg.SpawningPool),
      GetAtLeast(1, Zerg.Extractor),
      GetAtLeast(14, Zerg.Drone)),
    new RequireMiningBases(2),
    new BuildOrder(
      GetAtLeast(1, Zerg.Lair),
      GetAtLeast(4, Zerg.Zergling),
      GetAtLeast(15, Zerg.Drone),
      GetAtLeast(1, Zerg.Spire),
      GetAtLeast(16, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(1, Zerg.CreepColony),
      initialBefore = new Build(GetAtLeast(1, Zerg.CreepColony))),
    new BuildOrder(
      GetAtLeast(3, Zerg.Overlord)))
  
  override def blueprints: Seq[Blueprint] = Vector(
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)),
    new Blueprint(this, building = Some(Zerg.CreepColony), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.hugTownHall)))

  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Zerg.Mutalisk),
    new TrainContinuously(Zerg.SunkenColony),
    new TrainContinuously(Zerg.Drone, 16),
    new Build(GetAtLeast(5, Zerg.Overlord)),
    new Trigger(
      new UnitsAtLeast(4, Zerg.Overlord, complete = true),
      new BuildGasPumps)
  )
}
