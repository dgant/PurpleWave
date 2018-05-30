package Planning.Plans.GamePlans.Zerg.ZvE

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.BuildRequests.RequestAtLeast
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.SafeAtHome
import Planning.Plans.Scouting.{FoundEnemyBase, Scout}
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}

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
  
  override def defaultAttackPlan: Plan = new Attack(Zerg.Mutalisk)
  
  override def defaultBuildOrder: Plan = new Parallel (
    new BuildOrder(
      RequestAtLeast(9, Zerg.Drone),
      RequestAtLeast(2, Zerg.Overlord),
      RequestAtLeast(13, Zerg.Drone),
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
      RequestAtLeast(24, Zerg.Drone)), // 20 drones -- if we use larva intelligently we can fit one more in here
    
    // For whatever reason, this is required in order to not build it too early
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire),
      new RequireBases(3)),
    new BuildOrder(RequestAtLeast(7, Zerg.Mutalisk))
  )
  
  override def defaultScoutPlan = new If(
    new If(
      new Not(new FoundEnemyBase),
      new Scout(15) { scouts.get.unitMatcher.set(Zerg.Overlord) }))
  
  override def defaultSupplyPlan: Plan = NoPlan()
  override def buildPlans: Seq[Plan] = Vector(
    new KillNeutralBlocker,
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire, complete = true),
      new Parallel(
        
        new If(
          new Or(
            new EnemyUnitsAtMost(0, Zerg.Mutalisk),
            new UnitsAtLeast(10, Zerg.Mutalisk)),
          // We would like to build another Drone here but this is technically difficult due to limitations on how BuildOrder works
          new Build(RequestAtLeast(3, Zerg.Zergling)),
          new BuildOrder(RequestAtLeast(6, Zerg.Overlord))),
        
        super.defaultSupplyPlan,
        
        new If(
          new SafeAtHome,
          new TrainContinuously(Zerg.Drone, 18),
          new TrainContinuously(Zerg.Drone, 10)),
  
        new If(
          new EnemyHasShownWraithCloak,
          new UpgradeContinuously(Zerg.OverlordSpeed)),
  
        new TrainMatchingRatio(Zerg.Scourge, 0, 20,
          Seq(
            MatchingRatio(Terran.Wraith, 2.0),
            MatchingRatio(Terran.Battlecruiser, 4.0),
            MatchingRatio(Protoss.Carrier, 6.0))),
  
        new TrainMatchingRatio(Zerg.Devourer, 0, 20, Seq(MatchingRatio(Protoss.Corsair, 0.25))),
        
        new If(
          new UnitsAtLeast(3, Zerg.Mutalisk),
          new TrainMatchingRatio(Zerg.Scourge, 0, 20, Seq(MatchingRatio(Zerg.Mutalisk, 2.0)))),
  
        new If(
          new UnitsAtLeast(24, Zerg.Mutalisk),
          new Parallel(
            new TrainContinuously(Zerg.Guardian, 4),
            new Build(
              RequestAtLeast(1, Zerg.QueensNest),
              RequestAtLeast(1, Zerg.Hive),
              RequestAtLeast(1, Zerg.GreaterSpire)))),
        
        new If(
          new And(
            new UpgradeComplete(Zerg.AirArmor, 2),
            new Not(new UpgradeComplete(Zerg.AirDamage, 3))),
          new UpgradeContinuously(Zerg.AirDamage),
          new UpgradeContinuously(Zerg.AirArmor)),
        
        new If(
          new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
          new TrainContinuously(Zerg.Mutalisk),
          new TrainContinuously(Zerg.Drone, 25)),
  
        
        
        new If(
          new And(
            new UnitsAtLeast(18, Zerg.Drone),
            new FrameAtLeast(GameTime(6, 30)())), // Psi Disruptor won't die before this
          new Build(RequestAtLeast(3, Zerg.Extractor))),
        
        new If(
          new And(
            new MineralsAtLeast(500),
            new UnitsAtLeast(6, Zerg.Larva)),
          new Parallel(
            new If(
              new Check(() => With.units.countOurs(Zerg.SporeColony) > 4 * With.units.countOurs(Zerg.SunkenColony) + 3),
              new TrainContinuously(Zerg.SunkenColony),
              new TrainContinuously(Zerg.SporeColony)),
            new TrainContinuously(Zerg.Zergling, 50, 3),
            new Build(RequestAtLeast(1, Zerg.EvolutionChamber)),
            new If(
              new UnitsAtLeast(24, Zerg.Drone),
              new TrainContinuously(Zerg.CreepColony, 2)))
        ))))
}
