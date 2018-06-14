package Planning.Plans.GamePlans.Zerg.ZvE

import Macro.BuildRequests.{GetAtLeast, GetUpgrade}
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
import Planning.Plans.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.NineHatchLings

class NineHatchLings extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(NineHatchLings)
  
  override def defaultBuildOrder: Plan = new Parallel(
    new BuildOrder(GetAtLeast(9, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(2, Zerg.Hatchery),
      initialBefore = new ExtractorTrick),
    new BuildOrder(
      GetAtLeast(10, Zerg.Drone),
      GetAtLeast(2, Zerg.Hatchery),
      GetAtLeast(1, Zerg.SpawningPool),
      GetAtLeast(11, Zerg.Drone),
      GetAtLeast(2, Zerg.Overlord),
      GetAtLeast(6, Zerg.Zergling)))
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new EnemiesAtLeast(1, UnitMatchOr(Zerg.Spire, Zerg.Mutalisk, Zerg.Hydralisk))),
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
        new EnemiesAtLeast(1, Zerg.Spire, complete = true),
        new EnemiesAtLeast(1, Zerg.Mutalisk),
        new EnemiesAtLeast(1, Terran.Vulture))),
    
    new TrainContinuously(Zerg.Drone, 9),
    new TrainContinuously(Zerg.Zergling),
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
        new Build(GetAtLeast(1, Zerg.Extractor), GetUpgrade(Zerg.ZerglingSpeed)),
        new If(
          new MineralsAtLeast(280),
          new Build(GetAtLeast(3, Zerg.Hatchery)))))
  )
}
