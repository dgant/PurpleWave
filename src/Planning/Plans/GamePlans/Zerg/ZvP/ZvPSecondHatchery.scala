package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpRatio}
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Always
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Protoss, Zerg}

class ZvPSecondHatchery extends GameplanTemplate {

  override val activationCriteria: Predicate = new Always
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Zerg.Hatchery))

  class OverpoolDrones extends Predicate {
    var locked = false
    var drones = false

    override def isComplete: Boolean = {
      if (locked) return drones
      if (With.fingerprints.gatewayFirst.matches || With.fingerprints.cannonRush.matches || With.fingerprints.gatewayFe.matches) {
        locked = true
        drones = false
      } else if (With.fingerprints.forgeFe.matches) {
        locked = true
        drones = true
      } else if (With.units.existsOurs(UnitMatchAnd(Zerg.SpawningPool, UnitMatchComplete))) {
        locked = true
        drones = false
      }
      drones
    }
  }

  override def emergencyPlans: Seq[Plan] = Seq(
    new ZergReactionVsWorkerRush
  )

  override def buildOrderPlan: Plan = new Parallel(
    new If(
      new EnemyRecentStrategy(With.fingerprints.proxyGateway, With.fingerprints.cannonRush),
      new Parallel(
        new BuildOrder(
          Get(9, Zerg.Drone),
          Get(2, Zerg.Overlord),
          Get(Zerg.SpawningPool),
          Get(12, Zerg.Drone)),
        new If(
          new OverpoolDrones,
          new BuildOrder(Get(15, Zerg.Drone)),
          new BuildOrder(Get(6, Zerg.Zergling))),
        new RequireMiningBases(2)),
      new Parallel(
        new BuildOrder(
          Get(9, Zerg.Drone),
          Get(2, Zerg.Overlord),
          Get(12, Zerg.Drone)),
        new RequireMiningBases(2)),
  ))

  override def buildPlans: Seq[Plan] = Seq(
    new If(
      new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.cannonRush, With.fingerprints.proxyGateway),
      new PumpRatio(Zerg.Zergling, 6, 18, Seq(Enemy(Protoss.Zealot, 4.0)))),
    new Pump(Zerg.Drone, 13)
  )

}
