package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.Latch
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Zerg.GameplanZerg
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Predicates.SafeAtHome
import ProxyBwapi.Races.{Protoss, Zerg}

class ZvPReactive extends GameplanZerg {
  
  override def defaultAttackPlan: Plan = new Attack
  override def defaultScoutPlan: Plan = NoPlan()
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(11, Zerg.Drone),
    RequestAtLeast(6, Zerg.Zergling))
    
  object EnemyOpeningUnknown
  object EnemyOpeningOneBase
  object EnemyOpeningTwoBase
  var enemyOpening: Any = EnemyOpeningUnknown
  def detectEnemyStrategy() {
    if (With.geography.enemyBases.size > 1
    || With.units.enemy.exists(_.is(Protoss.PhotonCannon))
    || With.units.enemy.exists(_.is(Protoss.Forge))) {
      enemyOpening = EnemyOpeningTwoBase
    }
    else if (
      With.units.enemy.exists(_.is(Protoss.Gateway))
      || With.units.enemy.exists(_.is(Protoss.Zealot))) {
      enemyOpening = EnemyOpeningOneBase
    }
  }
  
  private class EnemyOpeningKnown extends Plan {
    override def isComplete: Boolean = enemyOpening != EnemyOpeningUnknown
  }
  private class EnemyOpenedOneBase extends Plan {
    override def isComplete: Boolean = enemyOpening == EnemyOpeningOneBase
  }
  
  override def doneWithEarlyGame: Plan = new Latch(new MiningBasesAtLeast(3))
  
  override def earlyGame: Plan = new Parallel(
    new Build(RequestAtLeast(9, Zerg.Drone)),
    new RequireMiningBases(2),
    new Trigger(
      new EnemyOpeningKnown,
      new If(
        new EnemyOpenedOneBase,
        new Parallel(
          new TrainContinuously(Zerg.SunkenColony),
          new BuildSunkensAtNatural(3),
          new If(
            new And(
              new Latch(new UnitsAtLeast(3, Zerg.SunkenColony, complete = true)),
              new SafeAtHome),
            new TrainWorkersContinuously),
          goOffense,
          new Build(
            RequestAtLeast(1, Zerg.Extractor),
            RequestAtLeast(1, Zerg.Lair),
            RequestUpgrade(Zerg.ZerglingSpeed),
            RequestAtLeast(1, Zerg.HydraliskDen),
            RequestTech(Zerg.LurkerMorph)),
          new If(
            new UnitsAtLeast(3, Zerg.Lurker, complete = true),
            new RequireMiningBases(3)),
          new Build(RequestAtLeast(5, Zerg.Hatchery)
        )),
        new Parallel(
          new Build(RequestAtLeast(13, Zerg.Drone)),
          new RequireMiningBases(3),
          new Build(RequestAtLeast(25, Zerg.Drone)))
      )
    )
  )
  
  override def onUpdate(): Unit = {
    detectEnemyStrategy()
    super.onUpdate()
  }
}
