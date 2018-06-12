package Planning.Plans.GamePlans.Zerg.ZvE

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchOr
import Planning.Plan
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, ExtractorTrick, TrainContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Matchup.EnemyIsZerg
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.NineHatchLings

class NineHatchLings extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(NineHatchLings)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(RequestAtLeast(9, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(2, Zerg.Hatchery),
      initialBefore = new ExtractorTrick),
    new BuildOrder(
      RequestAtLeast(10, Zerg.Drone),
      RequestAtLeast(2, Zerg.Hatchery),
      RequestAtLeast(1, Zerg.SpawningPool),
      RequestAtLeast(11, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(6, Zerg.Zergling)))
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new EnemyUnitsAtLeast(1, UnitMatchOr(Zerg.Spire, Zerg.Mutalisk, Zerg.Hydralisk))),
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  override def defaultAttackPlan: Plan = new If(
    new Or(
      new UpgradeComplete(Zerg.ZerglingSpeed),
      new Not(new EnemyIsZerg)),
    new Attack,
    new Scout { scouts.get.unitMatcher.set(Zerg.Zergling) })
  
  override def buildPlans: Seq[Plan] = Vector(
    
    new If(
      new Or(
        new GasAtLeast(100),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1))),
      new CapGasAt(0),
      new CapGasAt(100)),
  
    new AllIn(
      new Or(
        new EnemyUnitsAtLeast(1, Zerg.Spire, complete = true),
        new EnemyUnitsAtLeast(1, Zerg.Mutalisk),
        new EnemyUnitsAtLeast(1, Terran.Vulture))),
    
    new TrainContinuously(Zerg.Drone, 9),
    new TrainContinuously(Zerg.Zergling),
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
        new Build(RequestAtLeast(1, Zerg.Extractor), RequestUpgrade(Zerg.ZerglingSpeed)),
        new If(
          new MineralsAtLeast(280),
          new Build(RequestAtLeast(3, Zerg.Hatchery)))))
  )
}
