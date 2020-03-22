package Planning.Plans.GamePlans.AllRaces

import Lifecycle.With
import Macro.BuildRequests.GetAnother
import Mathematics.PurpleMath
import Planning.Plans.Army.AttackWithWorkers
import Planning.Plans.Basic.Do
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.StandardGamePlan
import Planning.Plans.Macro.Automatic.{Gather, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Scouting.{ConsiderScoutingWithOverlords, ScoutWithWorkers}
import Planning.Predicates.Compound.{And, Check, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemiesAtMost, FoundEnemyBase}
import Planning.Predicates.Strategy.Employing
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchComplete, UnitMatchWorkers}
import Planning.UnitPreferences.UnitPreferClose
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.AllRaces.{WorkerRushContinuousProduction, WorkerRushImmediate, WorkerRushOnScout, WorkerRushOnSupplyBlock}

class WorkerRush extends Trigger {

  class TimeToAtack extends Latch(new Or(
    new And(new Employing(WorkerRushImmediate)),
    new And(new Employing(WorkerRushContinuousProduction)),
    new And(new Employing(WorkerRushOnScout), new FoundEnemyBase),
    new And(new Employing(WorkerRushOnSupplyBlock), new Latch(new Check(() => With.self.supplyUsed >= PurpleMath.clamp(With.self.supplyTotal, 18, 20))))
  ))

  lazy val timeToAtack = new TimeToAtack

  private class ExecuteRush extends Parallel(
    // We get confused because our builder is also our gatherer,
    // so we send it "in advance" but then we have no income
    new Do(() => With.blackboard.maxFramesToSendAdvanceBuilder = 0),
    new If(
      new Check(() => With.self.supplyUsed == With.self.supplyTotal),
      new Build(GetAnother(1, With.self.supplyClass))),
    new Pump(With.self.workerClass),
    new FollowBuildOrder,
    new ConsiderScoutingWithOverlords,
    new If(
      new And(new Not(new TimeToAtack), new Not(new FoundEnemyBase)),
      new If(
        new Employing(WorkerRushOnScout),
        new ScoutWithWorkers(maxScouts = PurpleMath.clamp(With.game.getStartLocations.size() - 2, 1, 2)),
        new ScoutWithWorkers)),
    new Gather {
      var seenFiveWorkers: Boolean = false
      override def onUpdate(): Unit = {
        seenFiveWorkers = seenFiveWorkers || With.units.countOurs(UnitMatchAnd(UnitMatchWorkers, UnitMatchComplete)) >= 5
        val goalWorkers =
          if (With.strategy.selectedCurrently.contains(WorkerRushContinuousProduction))
            3
          else if ( ! timeToAtack.isComplete)
            200
          else if (seenFiveWorkers)
            1
          else
            0
        if (workerLock.units.size > goalWorkers) {
          workerLock.release()
        }
        workerLock.unitCounter.set(new UnitCountBetween(1, goalWorkers))
        workerLock.unitPreference.set(UnitPreferClose(With.geography.home.pixelCenter))
        workerLock.canPoach.set(true)
        super.onUpdate()
      }
    },
    new If(timeToAtack, new AttackWithWorkers))
 
  private class TimeToEndTheRush extends And(
    new EnemiesAtMost(1, UnitMatchWorkers),
    new Or(
      new EnemiesAtLeast(1, Protoss.PhotonCannon, complete = true),
      new EnemiesAtLeast(1, Zerg.SunkenColony, complete = true)))
  
  predicate.set(new TimeToEndTheRush)
  before.set(new ExecuteRush)
  after.set(new StandardGamePlan)
}