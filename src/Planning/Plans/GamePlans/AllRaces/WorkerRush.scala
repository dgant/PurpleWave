package Planning.Plans.GamePlans.AllRaces

import Lifecycle.With
import Macro.BuildRequests.GetAnother
import Planning.Predicates.Compound.{And, Check}
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.UnitPreferences.UnitPreferClose
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.StandardGamePlan
import Planning.Plans.Macro.Automatic.{Gather, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemiesAtMost, UnitsAtLeast}
import ProxyBwapi.Races.{Protoss, Zerg}

class WorkerRush extends Trigger {
  
  private class ExecuteRush extends Parallel(
    // We get confused because our builder is also our gatherer,
    // so we send it "in advance" but then we have no income
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = 0),
    new If(
      new Check(() => With.self.supplyUsed == With.self.supplyTotal),
      new Build(GetAnother(1, With.self.supplyClass))
    ),
    new Pump(With.self.workerClass),
    new FollowBuildOrder,
    new Trigger(
      new UnitsAtLeast(5, UnitMatchWorkers, complete = true),
      initialAfter = new Gather {
        workerLock.unitCounter.set(UnitCountExactly(1))
        workerLock.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
        //workers.interruptable.set(false) // Note that this means we can't build more workers until another Probe spawns. But that also means Probes aren't dying.
      }),
    new AttackWithWorkers)
 
  private class TimeToEndTheRush extends And(
    new EnemiesAtMost(1, UnitMatchWorkers),
    new Or(
      new EnemiesAtLeast(1, Protoss.PhotonCannon, complete = true),
      new EnemiesAtLeast(1, Zerg.SunkenColony, complete = true)))
  
  predicate.set(new TimeToEndTheRush)
  before.set(new ExecuteRush)
  after.set(new StandardGamePlan)
}