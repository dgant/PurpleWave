package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.{FoundEnemyBase, Scout}
import ProxyBwapi.Races.Zerg

class ZergSparkle extends GameplanModeTemplate {
  
  override def aggression: Double = 0.6
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new Attack(Zerg.Zergling, UnitCountOne))
  
  override def defaultAttackPlan: Plan = new Attack(Zerg.Mutalisk)
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(13, Zerg.Drone),
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(1, Zerg.Extractor),
      RequestAtLeast(15, Zerg.Drone)),
    new RequireMiningBases(2),
    new BuildOrder(
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(19, Zerg.Drone),
      RequestAtLeast(1, Zerg.Spire),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(23, Zerg.Drone),
      RequestAtLeast(4, Zerg.Overlord),
      RequestAtLeast(2, Zerg.Extractor),
      RequestAtLeast(6, Zerg.Mutalisk),
      RequestUpgrade(Zerg.AirArmor),
      RequestAtLeast(10, Zerg.Mutalisk)),
    new RequireMiningBases(3))

  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new Not(new FoundEnemyBase),
      new Scout {
        scouts.get.unitMatcher.set(Zerg.Overlord)
        scouts.get.unitCounter.set(UnitCountEverything)
      }),
    new If(
      new Check(() => With.self.gas >= With.self.minerals),
      new TrainContinuously(Zerg.Mutalisk),
      new TrainContinuously(Zerg.Drone, 24)),
    new Build(RequestAtLeast(3, Zerg.Extractor)))
}
