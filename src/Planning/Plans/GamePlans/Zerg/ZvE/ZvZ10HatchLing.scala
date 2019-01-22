package Planning.Plans.GamePlans.Zerg.ZvE

import Macro.BuildRequests.Get
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.{PumpJustEnoughScourge, PumpMutalisks}
import Planning.Plans.Macro.Automatic.{ExtractorTrick, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Scouting.Scout
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyIsZerg}
import Planning.UnitMatchers.UnitMatchOr
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Zerg
import Strategery.Strategies.Zerg.ZvZ10HatchLing

class ZvZ10HatchLing extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvZ10HatchLing)

  // https://liquipedia.net/starcraft/10_Hatch_(vs._Zerg)
  override def buildOrderPlan: Plan = new Parallel(
    new BuildOrder(Get(9, Zerg.Drone)),
    new Trigger(
      new UnitsAtLeast(2, Zerg.Hatchery),
      initialBefore = new ExtractorTrick),
    new BuildOrder(
      Get(10, Zerg.Drone),
      Get(2, Zerg.Hatchery),
      Get(Zerg.SpawningPool),
      Get(11, Zerg.Drone),
      Get(Zerg.Extractor),
      Get(2, Zerg.Overlord),
      Get(12, Zerg.Drone),
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
    new AllIn(
      new And(
        new UnitsAtMost(0, Zerg.Spire, complete = true),
        new Or(
          new EnemiesAtLeast(1, Zerg.Spire, complete = true),
          new EnemiesAtLeast(1, Zerg.Mutalisk)))),
    
    new Pump(Zerg.Drone, 9),
    new PumpJustEnoughScourge,
    new PumpMutalisks,
    new FlipIf(
      new UnitsAtLeast(12, Zerg.Zergling),
      new Pump(Zerg.Zergling),
      new Build(
        Get(Zerg.ZerglingSpeed),
        Get(Zerg.Lair),
        Get(Zerg.Spire))),
  )
}
