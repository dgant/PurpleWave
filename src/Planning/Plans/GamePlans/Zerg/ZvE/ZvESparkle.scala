package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.Get
import Micro.Agency.Intention
import Planning.Plans.Army.{Aggression, Attack}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireBases
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Economy.MineralsAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountEverything
import Planning.UnitMatchers.{MatchOr, MatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}
import Strategery.Sparkle
import Utilities.GameTime

class ZvESparkle extends GameplanTemplate {

  override val activationCriteria: Predicate = new Check(() => Sparkle.matches)
  
  class KillNeutralBlocker extends Plan() {
    val killers = new LockUnits(this)
    killers.matcher = MatchWarriors
    killers.counter = CountEverything
    override def onUpdate() {
      val targets = With.geography.ourZones.flatMap(_.units.filter(Neutral.PsiDisruptor))
      if (targets.isEmpty) return
      killers.acquire(this)
      killers.units.foreach(killer => killer.intend(this, new Intention {
        toAttack = targets.headOption
      }))
    }
  }
  
  override def aggressionPlan: Plan = new Aggression(0.8)
  
  override def attackPlan: Plan = new If(new UnitsAtLeast(1, Zerg.Mutalisk, complete = true), new Attack)
  
  override def buildOrderPlan: Plan = new Parallel (
    new BuildOrder(
      Get(9, Zerg.Drone),
      Get(2, Zerg.Overlord),
      Get(13, Zerg.Drone),
      Get(1, Zerg.SpawningPool), // -1 Drone
      Get(1, Zerg.Extractor), // -2 Drone
      Get(15, Zerg.Drone)),
    new RequireBases(2), // -3 Drone
    new BuildOrder(
      Get(1, Zerg.Lair),
      Get(21, Zerg.Drone),
      Get(1, Zerg.Spire), // -4 Drone
      Get(2, Zerg.Overlord), // TODO: We hit 120/58 while trying to build 6th Mutalisk -- get Extractor a little earlier
      Get(23, Zerg.Drone),
      Get(4, Zerg.Overlord), // 34 supply available
      Get(2, Zerg.Extractor), // -5 Drone
      Get(24, Zerg.Drone)), // 20 drones -- if we use larva intelligently we can fit one more in here
    
    // For whatever reason, this is required in order to not build it too early
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire),
      new RequireBases(3)),
    new BuildOrder(Get(7, Zerg.Mutalisk))
  )
  
  override def scoutPlan = NoPlan()
  override def supplyPlan: Plan = NoPlan()
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(12, Zerg.Drone),
      new CapGasAtRatioToMinerals(1.0, 400)),
    new KillNeutralBlocker,
    new Trigger(
      new UnitsAtLeast(1, Zerg.Spire, complete = true),
      new Parallel(
        
        new If(
          new Or(
            new EnemiesAtMost(0, Zerg.Mutalisk),
            new UnitsAtLeast(10, Zerg.Mutalisk)),
          // We would like to build another Drone here but this is technically difficult due to limitations on how BuildOrder works
          new Build(Get(3, Zerg.Zergling)),
          new BuildOrder(Get(6, Zerg.Overlord))),
        
        super.supplyPlan,
        
        new If(
          new SafeAtHome,
          new Pump(Zerg.Drone, 18),
          new Pump(Zerg.Drone, 10)),
  
        new If(
          new EnemyHasShownWraithCloak,
          new UpgradeContinuously(Zerg.OverlordSpeed)),
  
        new PumpRatio(Zerg.Scourge, 0, 20,
          Seq(
            Enemy(Terran.Wraith, 2.0),
            Enemy(Terran.Battlecruiser, 4.0),
            Enemy(Protoss.Carrier, 6.0))),
  
        new PumpRatio(Zerg.Devourer, 0, 20, Seq(Enemy(Protoss.Corsair, 0.25))),
        new PumpRatio(Zerg.Scourge, 0, 8, Seq(Enemy(Protoss.Corsair, 2.0))),
        
        new If(
          new UnitsAtLeast(3, Zerg.Mutalisk),
          new PumpRatio(Zerg.Scourge, 0, 20, Seq(Enemy(Zerg.Mutalisk, 2.0)))),
  
        new If(
          new And(
            new UnitsAtLeast(12, Zerg.Mutalisk),
            new Or(
              new UnitsAtLeast(24, Zerg.Mutalisk),
              new EnemiesAtLeast(20, Terran.Marine),
              new EnemiesAtLeast(6, MatchOr(Terran.Goliath, Terran.MissileTurret, Protoss.PhotonCannon)),
              new EnemiesAtLeast(2, MatchOr(Zerg.SporeColony, Terran.Bunker)))),
          new Parallel(
            new Pump(Zerg.Guardian, 4),
            new Build(
              Get(1, Zerg.QueensNest),
              Get(1, Zerg.Hive),
              Get(1, Zerg.GreaterSpire)))),
        new If(
          new Or(
            new UnitsAtLeast(1, Zerg.GreaterSpire),
            new UnitsAtLeast(0, Zerg.Hive)),
          new If(
            new And(
              new UpgradeComplete(Zerg.AirArmor, 2),
              new Not(new UpgradeComplete(Zerg.AirDamage, 3))),
            new UpgradeContinuously(Zerg.AirDamage),
            new UpgradeContinuously(Zerg.AirArmor))),
        
        new If(
          new Check(() => With.self.gas >= Math.min(100, With.self.minerals)),
          new Pump(Zerg.Mutalisk),
          new Pump(Zerg.Drone, 25)),
        
        new If(
          new And(
            new UnitsAtLeast(16, Zerg.Drone),
            new FrameAtLeast(GameTime(6, 45)())), // Psi Disruptor won't die before this
          new Build(Get(3, Zerg.Extractor))),
        
        new If(
          new And(
            new MineralsAtLeast(500),
            new UnitsAtLeast(6, Zerg.Larva)),
          new Parallel(
            new If(
              new Check(() => With.units.countOurs(Zerg.SporeColony) > 4 * With.units.countOurs(Zerg.SunkenColony) + 3),
              new Pump(Zerg.SunkenColony),
              new Pump(Zerg.SporeColony)),
            new Pump(Zerg.Zergling, 50, 3),
            new Build(Get(1, Zerg.EvolutionChamber)),
            new If(
              new UnitsAtLeast(24, Zerg.Drone),
              new Pump(Zerg.CreepColony, 2)))
        ))))
}
