package Planning.Plans.GamePlans.Zerg.ZvE

import Macro.BuildRequests.Get
import Planning.Predicates.Compound.Not
import Planning.UnitMatchers.UnitMatchOr
import Planning.{Plan, Predicate}
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, ExtractorTrick, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Strategy.{Employing, EnemyIsZerg}
import Planning.Predicates.Milestones.{EnemiesAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.NineHatchLings

class NineHatchLings extends GameplanTemplate {
  
  override val activationCriteria: Predicate = new Employing(NineHatchLings)
  
  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(Get(9, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(2, Zerg.Hatchery),
      initialBefore = new ExtractorTrick),
    new BuildOrder(
      Get(10, Zerg.Drone),
      Get(2, Zerg.Hatchery),
      Get(1, Zerg.SpawningPool),
      Get(11, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(6, Zerg.Zergling)))
  
  override def scoutPlan: Plan = new If(
    new Not(new EnemiesAtLeast(1, UnitMatchOr(Zerg.Spire, Zerg.Mutalisk, Zerg.Hydralisk))),
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  override def attackPlan: Plan = new If(
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
    
    new Pump(Zerg.Drone, 9),
    new Pump(Zerg.Zergling),
    new Trigger(
      new UnitsAtLeast(1, Zerg.SpawningPool, complete = true),
      new Parallel(
        new Build(Get(1, Zerg.Extractor), Get(Zerg.ZerglingSpeed)),
        new If(
          new MineralsAtLeast(280),
          new Build(Get(3, Zerg.Hatchery)))))
  )
}
