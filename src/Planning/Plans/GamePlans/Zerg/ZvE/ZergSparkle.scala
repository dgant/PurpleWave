package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import Planning.Plans.Scouting.{ChillOverlords, FoundEnemyBase, Scout}
import ProxyBwapi.Races.{Neutral, Zerg}

class ZergSparkle extends GameplanModeTemplate {
  
  class KillNeutralBlocker extends Plan() {
    val killers = new LockUnits
    killers.unitMatcher.set(Zerg.Zergling)
    killers.unitCounter.set(UnitCountEverything)
    override def onUpdate() {
      val targets = With.geography.ourZones.flatMap(_.units.filter(_.is(Neutral.PsiDisruptor)))
      if (targets.isEmpty) return
      killers.acquire(this)
      killers.units.foreach(killer => killer.agent.intend(this, new Intention {
        toAttack = targets.headOption
      }))
    }
  }
  
  override def aggression: Double = 0.8
  
  override def defaultScoutPlan: Plan = new If(
    new Not(new FoundEnemyBase),
    new Attack(Zerg.Zergling, UnitCountOne))
  
  override def defaultAttackPlan: Plan = new Attack(Zerg.Mutalisk)
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(12, Zerg.Drone),
      RequestAtLeast(1, Zerg.SpawningPool), // -1 Drone
      RequestAtLeast(1, Zerg.Extractor), // -2 Drone
      RequestAtLeast(15, Zerg.Drone)),
    new RequireBases(2), // -3 Drone
    new BuildOrder(
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(21, Zerg.Drone),
      RequestAtLeast(1, Zerg.Spire), // -4 Drone
      RequestAtLeast(2, Zerg.Overlord), // TODO: We hit 120/58 while trying to build 6th Mutalisk -- get Extractor a little earlier
      RequestAtLeast(23, Zerg.Drone),
      RequestAtLeast(4, Zerg.Overlord), // 34 supply available
      RequestAtLeast(2, Zerg.Extractor), // -5 Drone
      RequestAtLeast(24, Zerg.Drone)), // 20 drones
    new RequireBases(3),
    new BuildOrder(RequestAtLeast(7, Zerg.Mutalisk))
  )
  
  override def defaultOverlordPlan = new If(
    new If(
      new FoundEnemyBase,
      new ChillOverlords,
      new Scout(8) { scouts.get.unitMatcher.set(Zerg.Overlord) }))
  
  override def defaultSupplyPlan: Plan = NoPlan()
  override def buildPlans: Seq[Plan] = Vector(
    new KillNeutralBlocker,
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire, complete = true),
      new Parallel(
        new BuildOrder(RequestAtLeast(1, Zerg.Zergling)),// 0 supply left
        super.defaultSupplyPlan,
        new TrainContinuously(Zerg.Drone, 10),
        new UpgradeContinuously(Zerg.AirArmor),
        new If(
          new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
          new TrainContinuously(Zerg.Mutalisk),
          new TrainContinuously(Zerg.Drone, 25)),
        new Build(RequestAtLeast(3, Zerg.Extractor)))
    )
  )
    
}
