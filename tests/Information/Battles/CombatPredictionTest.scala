package Information.Battles.Prediction.Simulation

import Information.Battles.Battles
import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.{Battle, Hysteresis}
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.Pixel
import Planning.Blackboard
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Players.{PlayerInfo, PlayerProxy}
import ProxyBwapi.Bullets.BulletInfo
import Micro.Coordination.DamageTracker
import Performance.Cache
import ProxyBwapi.Upgrades.{Upgrade, UpgradeTypes}
import bwapi.BulletType
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import ProxyBwapi.UnitInfo.{DamageDealt, DamageVolley, FriendlyUnitInfo}
import Utilities.Property

import scala.collection.mutable.{ArrayBuffer, HashMap}

object CombatPredictionTest extends App {
  // Synthetic BWAPI state; all damage, queue, checkpoint, timer and simulation methods are production code.
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def set(obj: AnyRef, name: String, value: Any): Unit = {
    val fields = Iterator.iterate[Class[_]](obj.getClass)(_.getSuperclass).takeWhile(_ != null).flatMap(_.getDeclaredFields)
    val field = fields.find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    field.setAccessible(true)
    field.set(obj, value.asInstanceOf[AnyRef])
  }
  def near(actual: Double, expected: Double): Unit = assert(Math.abs(actual - expected) < 1e-8, s"$actual != $expected")
  var passed = 0
  def check(name: String)(body: => Unit): Unit = { body; passed += 1; println("PASS " + name) }

  With.configuration = new Configuration
  With.configuration.simviz = false
  With.self = allocate(classOf[PlayerInfo])
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  set(With.proxy, "upgrades", UpgradeTypes.all.map(Upgrade))
  With.blackboard = allocate(classOf[Blackboard])
  set(With.blackboard, "aggressionRatio", new Property(1.0))
  With.battles = allocate(classOf[Battles])
  With.battles.byUnit = Map.empty

  def unit(hp: Double = 10.0, shields: Double = 0.0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_pixel", Pixel(64, 64))
    set(u, "_player", With.self)
    set(u, "damageQueue", new ArrayBuffer[u.DamageSource])
    val s = new Simulacrum(u)
    set(u, "simulacrum", s)
    s.player = With.self
    s.unitClass = Protoss.Dragoon
    s.hitPointsExact = hp
    s.shieldPointsExact = shields
    s.hitPointsInitial = Math.ceil(hp).toInt
    s.shieldPointsInitial = Math.ceil(shields).toInt
    s.subjectiveValue = 100
    s.measureHealth = true
    s.shouldScoreIfOurs = true
    s.alive = true
    s.behavior = BehaviorIdle
    u
  }
  def simulation(units: FriendlyUnitInfo*): Simulation = {
    val sim = new Simulation
    sim.shouldReset = false
    sim.battle = new Battle(isGlobal = false) {
      override lazy val simulated: Boolean = true
      override lazy val logSimulation: Boolean = false
    }
    units.foreach { u =>
      sim.realUnits += u
      sim.realUnitsOurs += u
      u.simulacrum.simulation = sim
    }
    sim
  }
  val shot = DamageVolley(5, 1, 0, 0, 0.5)

  check("fractional damage and per-hit minimum survive size/armor/hit chance") {
    near(shot(0).total, 2.5)
    near(DamageVolley(1, 2, 10, 0, 1)(0).total, 1.0)
    near(DamageVolley(1, 1, 10, 0, 1, 0.47)(0).total, 0.235)
    near(DamageVolley(6, 1, 0, 3, 1)(0).total, 6)
  }
  check("multi-hit shield depletion applies hull armor only to hits reaching hull") {
    val d = DamageVolley(8, 2, 1, 0, 1)(8)
    near(d.shields, 8)
    near(d.health, 7)
    val fractionalShield = DamageVolley(8, 1, 1, 0, 1)(0.5)
    near(fractionalShield.shields, 0)
    near(fractionalShield.health, 7)
  }
  check("simulated health retains halves through lethal damage") {
    val u = unit(10).simulacrum
    (1 to 3).foreach(_ => u.receiveDamage(shot(u.shieldPointsExact)))
    near(u.hitPointsExact, 2.5)
    assert(u.hitPoints == 3)
    u.receiveDamage(shot(0))
    near(u.hitPointsExact, 0)
  }
  check("retargeting preserves multiple projectiles and removes only the forecast") {
    With.frame = 100
    val target = unit().simulacrum
    val source = unit().simulacrum
    target.addDamage(source, 2, true, false, Some(shot), Some(1))
    target.addDamage(source, 3, true, false, Some(shot), Some(2))
    target.addDamage(source, 8, false, false, Some(shot))
    target.addDamage(source, 2, true, false, Some(shot), Some(1))
    assert(target.damageQueueSnapshot.size == 3)
    target.removeDamage(source)
    assert(target.damageQueueSnapshot.map(_.bulletId) == Vector(Some(1), Some(2)))
    target.clearDamage()
    assert(target.damageQueueSnapshot.isEmpty)
  }
  check("doom prediction consumes shields before evaluating later projectiles") {
    val target = unit().simulacrum
    target.hitPointsExact = 10
    target.shieldPointsExact = 10
    val source = unit().simulacrum
    val explosive = DamageVolley(10, 1, 0, 0, 0.5)
    target.addDamage(source, 1, true, false, Some(explosive), Some(1))
    target.addDamage(source, 2, true, false, Some(explosive), Some(2))
    assert(!target.doomed) // 10 shield damage, then 5 HP, not two full 10-damage hits.
    target.addDamage(source, 3, true, false, Some(explosive), Some(3))
    assert(target.doomFrameAbsolute == With.frame + 3)
  }
  check("sub-point shields cannot keep a target alive in doom prediction") {
    val target = unit(1, 1).simulacrum
    val source = unit().simulacrum
    val hit = DamageVolley(1, 1, 0, 100, 1)
    target.addDamage(source, 1, true, false, Some(hit), Some(1))
    target.addDamage(source, 2, true, false, Some(hit), Some(2))
    assert(target.doomFrameAbsolute == With.frame + 2)
  }
  check("pending damage uses impact time, survives a dead source and ignores forecasts") {
    With.frame = 200
    val target = unit(5)
    val source = unit().simulacrum
    source.alive = false
    target.addDamage(source, 2, true, false, Some(shot), Some(1))
    target.addDamage(source, 3, true, false, Some(shot), Some(2))
    target.addDamage(source, 1, false, false, Some(DamageVolley(100, 1, 0, 0, 1)))
    val sim = simulation(target)
    sim.resetPendingDamage(With.frame)
    target.clearDamage() // The game thread can refresh its queue after the snapshot.
    sim.step()
    near(target.simulacrum.hitPointsExact, 5)
    assert(!sim.battle.simulationComplete)
    sim.step()
    near(target.simulacrum.hitPointsExact, 2.5)
    assert(!sim.battle.simulationComplete)
    sim.step()
    near(target.simulacrum.hitPointsExact, 0)
    assert(!target.simulacrum.alive && sim.battle.simulationComplete)
  }
  check("end-of-frame deaths retain both units' actions") {
    val a = unit(1)
    val b = unit(1)
    val sim = simulation(a, b)
    var actions = 0
    a.simulacrum.behavior = new SimulacrumBehavior {
      val fighting = true
      def act(s: Simulacrum): Unit = { actions += 1; b.simulacrum.receiveDamage(DamageDealt(1, 0)) }
    }
    b.simulacrum.behavior = new SimulacrumBehavior {
      val fighting = true
      def act(s: Simulacrum): Unit = { actions += 1; a.simulacrum.receiveDamage(DamageDealt(1, 0)) }
    }
    sim.step()
    assert(actions == 2 && !a.simulacrum.alive && !b.simulacrum.alive)
  }
  check("health value measures fraction of durability including initial injury") {
    val marine = unit(20)
    marine.simulacrum.unitClass = Terran.Marine
    val dragoon = unit(50, 40)
    val sim = simulation(marine, dragoon)
    val checkpoint = new SimulationCheckpoint(sim, None)
    near(checkpoint.unitHealthValueMax(marine.simulacrum), 50)
    near(checkpoint.unitHealthValueMax(dragoon.simulacrum), 50)
    marine.simulacrum.receiveDamage(DamageDealt(2.5, 0))
    near(checkpoint.unitHealthValueLeft(marine.simulacrum), 43.75)
  }
  check("hysteresis timer resets on both decision transitions, not repeated decisions") {
    val u = unit()
    val battle = simulation(u).battle
    With.battles.byUnit = Map(u -> battle)
    val h = new Hysteresis(u)
    With.frame = 100
    u.agent.shouldFight = false
    h.update()
    assert(!h.lastShouldFight && h.decisionFrames == 0)
    With.frame = 140
    h.update()
    assert(h.decisionFrames == 40)
    u.agent.shouldFight = true
    h.update()
    assert(h.lastShouldFight && h.decisionFrames == 0)
    With.frame = 200
    h.update()
    assert(h.decisionFrames == 60)
  }
  def player(enemy: Boolean): PlayerInfo = {
    val p = allocate(classOf[PlayerInfo])
    set(p, "isUs", !enemy)
    set(p, "isEnemy", enemy)
    val bitmap = classOf[PlayerProxy].getDeclaredField("bitmap$0")
    bitmap.setAccessible(true)
    bitmap.setInt(p, -1)
    val upgrades = new HashMap[Upgrade, Cache[Int]]
    (Seq(Protoss.Shields) ++ Protoss.Dragoon.armorUpgrade ++ Terran.Marine.armorUpgrade).foreach(upgrades.put(_, new Cache(() => 0)))
    set(p, "upgradeLevelCaches", upgrades)
    p
  }
  def bullet(id: Int, source: Option[FriendlyUnitInfo], target: FriendlyUnitInfo, kind: BulletType = BulletType.Phase_Disruptor, miss: Boolean = false): BulletInfo = {
    val b = allocate(classOf[BulletInfo])
    set(b, "id", id)
    set(b, "exists", true)
    set(b, "sourceUnit", source)
    set(b, "targetUnit", Some(target))
    set(b, "targetPixel", Some(if (miss) Pixel(200, 200) else target.pixel))
    set(b, "bulletType", kind)
    set(b, "moving", true)
    set(b, "speed", 10.0)
    set(b, "distanceToTarget", 25.0)
    set(b, "bitmap$0", -1)
    b
  }
  val bulletSource = unit()
  val bulletTarget = unit()
  set(bulletSource, "_player", player(false))
  set(bulletTarget, "_player", player(true))
  set(bulletSource, "_unitClass", Protoss.Scout)
  set(bulletTarget, "_unitClass", Protoss.Dragoon)
  set(bulletSource, "_flying", true)
  set(bulletTarget, "_flying", true)
  set(bulletSource, "damageOnHitAirCache", new Cache(() => 14))
  set(bulletSource, "attacksAgainstAirCache", new Cache(() => 2))

  check("observed bullets count one hit each and survive loss of their source") {
    val tracker = new DamageTracker
    tracker.countBullet(bullet(10, Some(bulletSource), bulletTarget))
    tracker.countBullet(bullet(11, Some(bulletSource), bulletTarget))
    assert(bulletTarget.damageQueueSnapshot.size == 2)
    assert(bulletTarget.damageQueueSnapshot.forall(d => d.volley.hits == 1 && d.onFrame == With.frame + 3))
    tracker.countBullet(bullet(10, None, bulletTarget))
    assert(bulletTarget.damageQueueSnapshot.size == 2)
    bulletTarget.clearDamage()
  }
  check("missed and ambiguous Queen projectiles cannot declare a target doomed") {
    val tracker = new DamageTracker
    tracker.countBullet(bullet(12, Some(bulletSource), bulletTarget, miss = true))
    tracker.countBullet(bullet(13, Some(bulletSource), bulletTarget, BulletType.Queen_Spell_Carrier))
    assert(bulletTarget.damageQueueSnapshot.isEmpty)
  }
  check("Yamato keeps its own weapon damage when a normal attack is also predicted") {
    val tracker = new DamageTracker
    val target = unit()
    set(target, "_unitClass", Terran.Marine)
    set(target, "_player", player(true))
    tracker.countBullet(bullet(14, Some(bulletSource), target, BulletType.Yamato_Gun))
    target.addDamage(bulletSource, 20, false, false, Some(shot))
    near(target.damageQueueSnapshot.head.volley(0).total, 130) // Explosive vs small.
    assert(target.damageQueueSnapshot.size == 2)
  }
  println(s"$passed combat prediction checks passed")
}
