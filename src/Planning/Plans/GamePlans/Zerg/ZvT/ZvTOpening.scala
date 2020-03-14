package Planning.Plans.GamePlans.Zerg.ZvT

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZergIdeas.ScoutSafelyWithOverlord
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Terran, Zerg}
import Strategery.Strategies.Zerg.{ZvT12Hatch11Pool, ZvT12Hatch13Pool, ZvT9Pool}

class ZvTOpening extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(ZvT12Hatch13Pool, ZvT12Hatch11Pool, ZvT9Pool)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(1, Zerg.Lair))

  override def scoutWorkerPlan: Plan = new Parallel(
    new ScoutSafelyWithOverlord,
    new If(
      new Employing(ZvT9Pool),
      new ScoutOn(Zerg.SpawningPool),
      new ScoutOn(Zerg.Overlord, quantity = 2))
  )

  override def buildOrderPlan: Plan = new If(
    new Employing(ZvT9Pool),
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(Zerg.SpawningPool),
      Get(10, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(11, Zerg.Drone),
      Get(6, Zerg.Zergling)),
    new Parallel(
      new BuildOrder(
        Get(9, Zerg.Drone),
        Get(2, Zerg.Overlord),
        Get(12, Zerg.Drone)),
      new RequireMiningBases(2),
      new If(
          new And(
            new Employing(ZvT12Hatch13Pool),
            new Not(new EnemyStrategy(With.fingerprints.twoRax1113, With.fingerprints.bbs, With.fingerprints.fiveRax))),
        new BuildOrder(Get(14, Zerg.Drone))),
      new Build(Get(Zerg.SpawningPool))))

  override def buildPlans: Seq[Plan] = Seq(
    new EjectScout,
    new Pump(Zerg.SunkenColony),
    new If(
      new EnemyStrategy(With.fingerprints.oneRaxFE, With.fingerprints.twoRax1113),
      new RequireMiningBases(3)),
    new Build(Get(Zerg.Extractor)),
    new FlipIf(
      new EnemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.fiveRax),
      new Build(Get(Zerg.Lair)),
      new Build(Get(Zerg.ZerglingSpeed))),
    new If(
      new EnemyStrategy(With.fingerprints.oneRaxGas, With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.fiveRax),
      new BuildSunkensAtNatural(1)),
    new PumpRatio(Zerg.Zergling, 6, 12, Seq(Enemy(Terran.Marine, 2))),
    new PumpWorkers
  )
}
