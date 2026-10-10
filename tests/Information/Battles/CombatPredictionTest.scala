package Information.Battles.Prediction.Simulation

import Information.Battles.Battles
import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.{Battle, BattleJudgment, CombatGroup, Hysteresis, JudgmentModifiers}
import Tactic.Squads.Squad
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Physics.Force
import Information.Geography.Geography
import Information.Geography.Pathfinding.{GroundDistance, Paths}
import Information.Geography.Types.{Edge, Zone}
import Information.Grids.Grids
import Information.Grids.Movement.{GridWalkable, GridWalkableTerrain, GridUnwalkableUnits, GridFlowField, GridGroundDistance}
import Planning.Blackboard
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Players.{PlayerInfo, PlayerProxy}
import ProxyBwapi.Bullets.BulletInfo
import Micro.Coordination.DamageTracker
import Micro.Matchups.MatchupAnalysis
import Performance.Cache
import ProxyBwapi.Upgrades.{Upgrade, UpgradeTypes}
import bwapi.{BulletType, WeaponType}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
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

  def setWeapons(s: Simulacrum): Unit = {
    s.attacksAgainstAirCached    = s.unitClass.airDamageFactorRaw * s.unitClass.maxAirHitsRaw
    s.attacksAgainstGroundCached = if (s.unitClass == Protoss.Reaver) 1 else s.unitClass.groundDamageFactorRaw * s.unitClass.maxGroundHitsRaw
    s.pixelRangeAirCached        = s.unitClass.pixelRangeAir
    s.pixelRangeGroundCached     = s.unitClass.pixelRangeGround
  }
  def unit(hp: Int = 10, shields: Int = 0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "friendly", Some(u))
    set(u, "_squad", None)
    set(u, "_pixel", Pixel(64, 64))
    set(u, "_player", With.self)
    set(u, "damageQueue", new ArrayBuffer[u.DamageSource])
    val s = new Simulacrum(u)
    set(u, "simulacrum", s)
    s.player = With.self
    s.unitClass = Protoss.Dragoon
    setWeapons(s)
    s.hitPoints = hp
    s.shieldPoints = shields
    s.hitPointsInitial = hp
    s.shieldPointsInitial = shields
    s.subjectiveValue = 100
    s.measureHealth = true
    s.shouldScoreIfOurs = true
    s.alive = true
    s.behavior = BehaviorIdle
    u.matchups = new MatchupAnalysis(u) {
      override def engagedUpon: Boolean = false
      override def engagingOn: Boolean = false
    }
    u
  }
  def simulation(units: FriendlyUnitInfo*): Simulation = {
    val sim = new Simulation
    sim.shouldReset = false
    sim.battle = new Battle(unitsUs = units, isGlobal = false) {
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
  check("simulated health uses integer pools and rounds applied damage") {
    val u = unit(10).simulacrum
    (1 to 3).foreach(_ => u.receiveDamage(shot(u.shieldPoints)))
    assert(u.hitPoints == 1)
    u.receiveDamage(shot(0))
    near(u.hitPoints, 0)
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
    target.hitPoints = 10
    target.shieldPoints = 10
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
    val resetPendingDamage = classOf[Simulation].getDeclaredMethods.find(_.getName.endsWith("resetPendingDamage")).get
    resetPendingDamage.setAccessible(true)
    resetPendingDamage.invoke(sim, Int.box(With.frame))
    target.clearDamage() // The game thread can refresh its queue after the snapshot.
    sim.step()
    near(target.simulacrum.hitPoints, 5)
    assert(!sim.battle.simulationComplete)
    sim.step()
    near(target.simulacrum.hitPoints, 2)
    assert(!sim.battle.simulationComplete)
    sim.step()
    near(target.simulacrum.hitPoints, 0)
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
    near(checkpoint.unitHealthValueLeft(marine.simulacrum), 42.5)
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
  check("bloodlust survives fight permission and decays for 24 frames after actual engagement") {
    With.frame = 0
    val u = unit()
    var engaging = false
    var attacked = false
    u.matchups = new MatchupAnalysis(u) {
      override def engagingOn: Boolean = engaging
      override def engagedUpon: Boolean = attacked
    }
    val battle = simulation(u).battle
    val judgment = allocate(classOf[BattleJudgment])
    set(judgment, "scoreTotal", 0.05)
    set(judgment, "shouldFightGround", false)
    battle.judgement = Some(judgment)
    With.battles.byUnit = Map(u -> battle)
    val h = new Hysteresis(u)
    h.update()
    With.frame = 180
    h.update()
    near(h.bloodlustFrames, 180)
    set(judgment, "shouldFightGround", true)
    u.agent.shouldFight = true
    With.frame = 181
    h.update()
    near(h.bloodlustFrames, 180)
    With.frame = 200
    h.update()
    near(h.bloodlustFrames, 180)
    attacked = true
    assert(u.matchups.engaged)
    h.update()
    near(h.bloodlustFrames, 180)
    With.frame = 206
    attacked = false
    h.update()
    near(h.bloodlustFrames, 135)
    With.frame = 212
    engaging = true
    assert(u.matchups.engaged)
    h.update()
    near(h.bloodlustFrames, 90) // Re-entry must not restart decay.
    With.frame = 224
    h.update()
    near(h.bloodlustFrames, 0)
    With.frame = 230
    engaging = false
    set(judgment, "shouldFightGround", false)
    h.update()
    With.frame = 240
    h.update()
    near(h.bloodlustFrames, 10)
  }
  def scoreLosses(ourDamage: Int, enemyDamage: Int, enemyDead: Boolean = false): SimulationCheckpoint = {
    val us = unit(500)
    val enemy = unit(500)
    us.simulacrum.unitClass = Terran.Battlecruiser
    enemy.simulacrum.unitClass = Terran.Battlecruiser
    val sim = simulation(us, enemy)
    sim.realUnitsOurs -= enemy
    sim.realUnitsEnemy += enemy
    us.simulacrum.receiveDamage(DamageDealt(ourDamage, 0))
    enemy.simulacrum.receiveDamage(DamageDealt(enemyDamage, 0))
    enemy.simulacrum.alive = !enemyDead
    new SimulationCheckpoint(sim, None)
  }
  check("score combines 25 percent survival with 75 percent damage before comparing losses") {
    val damaged = scoreLosses(0, 250)
    near(damaged.lossEnemy, 37.5)
    near(damaged.totalScore, 1.0)
    val killed = scoreLosses(0, 500, enemyDead = true)
    near(killed.lossEnemy, 100)
    near(killed.totalScore, 1.0)
  }
  check("scores vary smoothly across zero own damage and zero total damage") {
    near(scoreLosses(0, 0).totalScore, 0)
    val noLoss = scoreLosses(0, 250).totalScore
    val smallLoss = scoreLosses(5, 250).totalScore
    near(smallLoss, 36.75 / 38.25)
    assert(noLoss > smallLoss && noLoss - smallLoss < 0.04)
    val tinyDamage = scoreLosses(0, 1).totalScore
    assert(tinyDamage > 0 && tinyDamage < 0.02)
    near(scoreLosses(100, 100).totalScore, 0)
    near(scoreLosses(5, 250).totalScore, -scoreLosses(250, 5).totalScore)
  }
  check("battle judgment has no automatic plus-one score for harmless simulations") {
    val sim = simulation()
    def judgmentScore: Double = {
      val judgment = allocate(classOf[BattleJudgment])
      set(judgment, "battle", sim.battle)
      judgment.scoreSim11
    }
    near(judgmentScore, 0)
    sim.battle.simulationCheckpoints += scoreLosses(0, 0)
    near(judgmentScore, 0)
    val damage = scoreLosses(0, 250)
    sim.battle.simulationCheckpoints += damage
    near(judgmentScore, damage.totalScore)
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
  check("score keeps both endpoints and is continuous where the denominator floor stops applying") {
    near(scoreLosses(0, 250).totalScore, 1)
    near(scoreLosses(250, 0).totalScore, -1)
    val below = scoreLosses(0, 66).totalScore // 9.9 loss against a floor of 10.
    val above = scoreLosses(0, 67).totalScore
    near(below, 0.99)
    near(above, 1.0)
    for (ours <- 0 to 500 by 25; theirs <- 0 to 500 by 25) {
      val score = scoreLosses(ours, theirs).totalScore
      assert(score >= -1 && score <= 1)
    }
  }

  With.mapTileWidth = 32
  With.mapTileHeight = 32
  With.mapTileArea = With.mapTileWidth * With.mapTileHeight
  With.mapPixelWidth = With.mapTileWidth * 32
  With.mapPixelHeight = With.mapTileHeight * 32
  With.game = allocate(classOf[bwapi.Game])
  set(With.game, "mapWidth", With.mapTileWidth)
  set(With.game, "mapHeight", With.mapTileHeight)
  set(With.game, "groundHeight", Array.ofDim[Int](With.mapTileWidth, With.mapTileHeight))
  With.grids = allocate(classOf[Grids])
  set(With.grids, "walkable", new GridWalkable)
  set(With.grids, "walkableTerrain", new GridWalkableTerrain)
  set(With.grids, "unwalkableUnits", new GridUnwalkableUnits)
  java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  With.geography = allocate(classOf[Geography])
  val zone = allocate(classOf[Zone])
  set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
  With.paths = allocate(classOf[Paths])

  def mover(sim: Simulation, at: Pixel, kind: UnitClass = Protoss.Dragoon, flying: Boolean = false): Simulacrum = {
    val s = unit(100).simulacrum
    s.unitClass = kind
    setWeapons(s)
    s.flying = flying
    s.topSpeed = 4
    s.topSpeedPossible = 4
    s.simulation = sim
    s.setPixel(at)
    sim.grid.populate(s)
    s
  }
  def gridConsistent(sim: Simulation): Unit = {
    sim.grid.populatedTiles.foreach { tile =>
      assert(tile.units.nonEmpty)
      assert(tile.occupancy == tile.units.map(s => if (s.flying) 0 else s.unitClass.occupancy).sum)
      tile.units.foreach(s => assert(s.gridTile.contains(tile) && s.pixel.tile == tile.tile && s.alive))
    }
  }
  check("flying movement tracks its actual cell and flying buildings consume no ground space") {
    val sim = simulation()
    val flyer = mover(sim, Pixel(48, 48), Terran.Barracks, flying = true)
    sim.grid.tryMove(flyer, Pixel(176, 48))
    assert(flyer.pixel == Pixel(52, 48))
    assert(flyer.gridTile.get.tile == Tile(1, 1))
    assert(flyer.gridTile.get.occupancy == 0)
    (1 to 4).foreach(_ => sim.grid.tryMove(flyer, Pixel(176, 48)))
    assert(flyer.lastTile.contains(Tile(1, 1)))
    gridConsistent(sim)
  }
  check("ground movement advances within a legal cell even when the next tile is full") {
    val sim = simulation()
    val moving = mover(sim, Pixel(48, 48))
    mover(sim, Pixel(80, 48))
    assert(sim.grid.tryForceV2(moving, Force(1, 0)))
    assert(moving.pixel == Pixel(52, 48))
    (1 to 2).foreach(_ => assert(sim.grid.tryForceV2(moving, Force(1, 0))))
    assert(moving.pixel == Pixel(60, 48))
    assert(!sim.grid.tryForceV2(moving, Force(1, 0)))
    assert(moving.pixel == Pixel(60, 48))
    gridConsistent(sim)
  }
  check("movement obeys ground occupancy and terrain while aircraft cross both") {
    val sim = simulation()
    val moving = mover(sim, Pixel(60, 48))
    mover(sim, Pixel(80, 48))
    With.grids.walkableTerrain.set(Tile(1, 0), false)
    With.grids.walkableTerrain.set(Tile(1, 2), false)
    With.grids.walkableTerrain.set(Tile(0, 1), false)
    try {
      sim.grid.tryMove(moving, Pixel(176, 48))
      assert(moving.pixel == Pixel(60, 48))
      val flyer = mover(sim, Pixel(60, 48), Protoss.Scout, flying = true)
      sim.grid.tryMove(flyer, Pixel(176, 48))
      assert(flyer.pixel == Pixel(64, 48))
      val terrainMover = mover(sim, Pixel(48, 32))
      assert(!sim.grid.tryForceV2(terrainMover, Force(0, -1)))
      assert(terrainMover.pixel == Pixel(48, 32))
      gridConsistent(sim)
    } finally java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  }
  check("lethal damage and suicide both release grid occupancy") {
    val sim = simulation()
    val moving = mover(sim, Pixel(60, 48))
    val blocker = mover(sim, Pixel(80, 48))
    assert(!sim.grid.tryForceV2(moving, Force(1, 0)))
    blocker.hitPoints = 0
    blocker.update()
    assert(blocker.gridTile.isEmpty)
    assert(sim.grid.tryForceV2(moving, Force(1, 0)))
    assert(moving.lastTile.contains(Tile(1, 1)))
    moving.alive = false
    moving.update()
    assert(sim.grid.populatedTiles.isEmpty)
    assert(sim.grid.tiles.forall(_.occupancy == 0))
  }
  check("movement clamps pixel positions at all four map edges") {
    val sim = simulation()
    for ((at, force) <- Seq(
      (Pixel(0, 48), Force(-1, 0)),
      (Pixel(With.mapPixelWidth - 1, 48), Force(1, 0)),
      (Pixel(48, 0), Force(0, -1)),
      (Pixel(48, With.mapPixelHeight - 1), Force(0, 1)))) {
      val moving = mover(sim, at)
      assert(!sim.grid.tryForceV2(moving, force))
      assert(moving.pixel == at)
    }
    gridConsistent(sim)
  }
  check("nearby goals across a tile edge do not overshoot") {
    val sim = simulation()
    val moving = mover(sim, Pixel(63, 48))
    sim.grid.tryMove(moving, Pixel(64, 48))
    assert(moving.pixel == Pixel(64, 48))
    assert(moving.lastTile.contains(Tile(1, 1)))
    gridConsistent(sim)
  }
  check("fallback can backtrack when every other adjacent tile is blocked") {
    val sim = simulation()
    val moving = mover(sim, Pixel(80, 48))
    moving.lastTile = Some(Tile(1, 1))
    mover(sim, Pixel(112, 48))
    sim.grid.relocateUnit(moving, Pixel(92, 48))
    With.grids.walkableTerrain.set(Tile(2, 0), false)
    With.grids.walkableTerrain.set(Tile(2, 2), false)
    try {
      sim.grid.tryMove(moving, Pixel(176, 48))
      assert(moving.pixel == Pixel(88, 48))
      gridConsistent(sim)
    } finally java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  }
  check("fallback takes the chosen adjacent step instead of repeating a blocked zone flow") {
    val destinationZone = allocate(classOf[Zone])
    val flow = new GridFlowField(Tile(5, 1))
    flow.set(Tile(1, 1), Force(0, -1))
    set(destinationZone, "flowField", flow)
    set(destinationZone, "bitmap$0", -1L)
    val zones = Array.fill(With.mapTileArea)(destinationZone)
    zones(Tile(1, 1).i) = zone
    set(With.geography, "_zoneByTile", zones)
    With.grids.walkableTerrain.set(Tile(1, 0), false)
    With.grids.walkableTerrain.set(Tile(1, 2), false)
    With.grids.walkableTerrain.set(Tile(0, 1), false)
    try {
      val sim = simulation()
      val moving = mover(sim, Pixel(48, 32))
      sim.grid.tryMove(moving, Pixel(176, 32))
      assert(moving.pixel == Pixel(52, 32), s"Clear sideways step was not taken: ${moving.pixel}")
      gridConsistent(sim)
    } finally {
      java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
      set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
    }
  }
  check("grid reset clears every occupied cell before repopulation") {
    val sim = simulation()
    val moving = mover(sim, Pixel(60, 48))
    sim.grid.tryMove(moving, Pixel(176, 48))
    sim.grid.reset()
    assert(sim.grid.populatedTiles.isEmpty)
    assert(sim.grid.tiles.forall(t => t.occupancy == 0 && t.units.isEmpty))
    sim.grid.populate(moving)
    gridConsistent(sim)
  }
  check("kiting uses distance beyond weapon range and still fires when cooldown expires") {
    val sim = simulation()
    val kiter = mover(sim, Pixel(480, 240))
    val target = mover(sim, Pixel(280, 240), Protoss.Zealot)
    kiter.player = player(false)
    target.player = player(true)
    kiter.cooldownLeft = 20
    kiter.topSpeed = 5
    kiter.cooldownTargeting = 72
    kiter.target = Some(target)
    kiter.targets.add(target)
    kiter.threat = Some(target)
    target.target = Some(kiter)
    kiter.pixelRangeGroundCached = 192.0
    kiter.pixelRangeAirCached = 192.0
    set(kiter, "damageOnHitGroundCache", new Cache(() => 20))
    target.pixelRangeGroundCached = 32.0
    kiter.behavior = BehaviorFight
    val start = kiter.pixel
    kiter.act()
    assert(kiter.moveGoal.exists(_.x > start.x))
    (1 to 20).foreach { _ => kiter.update(); kiter.act() }
    assert(target.hitPoints < 100, s"Next shot was delayed at distance ${kiter.pixelDistanceEdgeSimulation(target)}")
    gridConsistent(sim)
  }
  check("fallback retains stable tie order and only deprioritizes the previous tile") {
    val sim = simulation()
    val moving = mover(sim, Pixel(60, 47))
    mover(sim, Pixel(80, 47))
    sim.grid.tryMove(moving, Pixel(176, 47))
    assert(moving.pixel == Pixel(60, 43), s"Expected up, got ${moving.pixel}") // Up wins the up/down tie.
    sim.grid.relocateUnit(moving, Pixel(60, 47))
    moving.lastTile = Some(Tile(1, 0))
    sim.grid.tryMove(moving, Pixel(176, 47))
    assert(moving.pixel == Pixel(60, 51), s"Expected down, got ${moving.pixel}")
    gridConsistent(sim)
  }
  def congestion(): (Simulation, Simulacrum, Simulacrum, Simulacrum) = {
    val sim = simulation()
    val rear = mover(sim, Pixel(60, 48))
    val front = mover(sim, Pixel(80, 48))
    mover(sim, Pixel(112, 48))
    val target = mover(sim, Pixel(400, 48))
    rear.isFriendlyCached = true
    front.isFriendlyCached = true
    front.behavior = BehaviorFight
    front.target = Some(target)
    front.pixelRangeGroundCached = 192.0
    (sim, rear, front, target)
  }
  check("blocked entry makes an allied blocker yield laterally on the next frame") {
    val (sim, rear, front, _) = congestion()
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    assert(!sim.grid.tryYield(front)) // No dependence on same-frame update order.
    sim.grid.advanceFrame()
    assert(sim.grid.tryYield(front))
    assert(front.pixel.x == 80 && front.pixel.y != 48)
    (1 to 6).foreach { _ =>
      sim.grid.tryForceV2(rear, Force(1, 0))
      sim.grid.advanceFrame()
      sim.grid.tryYield(front)
    }
    assert(front.pixel.x == 80 && front.pixel.y < 32)
    assert(rear.pixel.x >= 64, s"Rear unit did not enter the vacated cell: ${rear.pixel}")
    gridConsistent(sim)
  }
  check("new requests do not overwrite pressure from the previous frame") {
    val (sim, rear, front, _) = congestion()
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.advanceFrame()
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    assert(sim.grid.tryYield(front))
  }
  check("congestion requests expire and reset cannot revive requests on empty cells") {
    val (sim, rear, front, _) = congestion()
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.advanceFrame()
    sim.grid.advanceFrame()
    assert(!sim.grid.tryYield(front))
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.remove(front)
    sim.grid.reset()
    sim.grid.populate(front)
    sim.grid.advanceFrame()
    assert(!sim.grid.tryYield(front))
  }
  check("opponents and blocked terrain do not ask allied occupants to yield") {
    val (sim, rear, front, _) = congestion()
    rear.isFriendlyCached = false
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.advanceFrame()
    assert(!sim.grid.tryYield(front))
    rear.isFriendlyCached = true
    With.grids.walkableTerrain.set(Tile(2, 1), false)
    try {
      assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
      sim.grid.advanceFrame()
      assert(!sim.grid.tryYield(front))
    } finally java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  }
  check("congestion cannot push a unit backwards when forward and lateral cells are blocked") {
    val (sim, rear, front, _) = congestion()
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.remove(rear) // The cell behind is now the only free alternative.
    With.grids.walkableTerrain.set(Tile(2, 0), false)
    With.grids.walkableTerrain.set(Tile(2, 2), false)
    try {
      sim.grid.advanceFrame()
      assert(!sim.grid.tryYield(front))
      assert(front.pixel == Pixel(80, 48))
    } finally java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  }
  check("movement update applies yielding even without an existing movement goal") {
    val (sim, rear, front, _) = congestion()
    front.cooldownMoving = 0
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.advanceFrame()
    front.update()
    assert(front.pixel.x == 80 && front.pixel.y != 48)
    gridConsistent(sim)
  }
  check("yielding respects attack wind-up and preserves the next firing opportunity") {
    val (sim, rear, front, target) = congestion()
    sim.grid.relocateUnit(target, Pixel(256, 48))
    front.player = player(false)
    target.player = player(true)
    front.cooldownTargeting = 72
    front.targets.add(target)
    set(front, "damageOnHitGroundCache", new Cache(() => 20))
    assert(!sim.grid.tryForceV2(rear, Force(1, 0)))
    sim.grid.advanceFrame()
    front.act()
    val afterFirstShot = target.hitPoints
    assert(afterFirstShot < 100)
    front.update()
    assert(front.pixel == Pixel(80, 48))
    (1 to 32).foreach { _ =>
      sim.grid.tryForceV2(rear, Force(1, 0))
      sim.grid.advanceFrame()
      front.act()
      front.update()
      assert(front.inRangeToAttackSimulation(target))
    }
    assert(front.pixel.y != 48)
    assert(target.hitPoints < afterFirstShot, "Lateral yielding delayed the next shot")
    gridConsistent(sim)
  }
  check("weapon snapshots retain capabilities until refreshed, including loaded-unit counts and range upgrades") {
    val u = unit()
    var hits = 4
    var range = 192.0
    set(u, "attacksAgainstAirCache", new Cache(() => hits))
    set(u, "attacksAgainstGroundCache", new Cache(() => hits))
    set(u, "pixelRangeAirCache", new Cache(() => range))
    set(u, "pixelRangeGroundCache", new Cache(() => range))
    u.simulacrum.snapshotWeapons()
    hits = 8
    range = 256.0
    With.frame += 1
    assert(u.attacksAgainstGround == 8 && u.pixelRangeGround == 256.0)
    assert(u.simulacrum.attacksAgainstAir == 4 && u.simulacrum.attacksAgainstGround == 4)
    near(u.simulacrum.pixelRangeAir, 192.0)
    near(u.simulacrum.pixelRangeGround, 192.0)
    u.simulacrum.snapshotWeapons()
    assert(u.simulacrum.attacksAgainstAir == 8 && u.simulacrum.attacksAgainstGround == 8)
    near(u.simulacrum.pixelRangeAir, 256.0)
    near(u.simulacrum.pixelRangeGround, 256.0)
  }
  check("movement evaluates its by-name goal once and skips it when immobile with recording disabled") {
    val sim = simulation()
    val moving = mover(sim, Pixel(48, 48))
    var evaluations = 0
    moving.move({ evaluations += 1; Pixel(176, 48) }, None)
    assert(evaluations == 1 && moving.moveGoal.contains(Pixel(176, 48)))
    moving.cooldownMoving = 0
    moving.topSpeed = 0
    moving.move({ evaluations += 1; Pixel(176, 48) }, None)
    assert(evaluations == 1)
  }
  check("ground distance preserves cross-zone edge minima, empty edges and unreachable distances") {
    class Distances extends GroundDistance { def exact(a: Tile, b: Tile): Int = groundTiles(a, b) }
    val distances = new Distances
    val destinationZone = allocate(classOf[Zone])
    set(destinationZone, "bitmap$0", -1L)
    set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(destinationZone))
    val a = Tile(1, 1)
    val b = Tile(5, 1)
    val edges = (0 until 3).map { _ =>
      val edge = allocate(classOf[Edge])
      set(edge, "distanceGrid", new GridGroundDistance())
      edge
    }.toVector
    try {
      set(destinationZone, "edges", Vector.empty[Edge])
      assert(distances.exact(a, b) == distances.impossiblyLargeDistanceTiles)
      set(destinationZone, "edges", edges)
      assert(distances.exact(a, b) == 2 * distances.impossiblyLargeDistanceTiles)
      for (i <- 0 until 50) {
        edges.zipWithIndex.foreach { case (edge, j) =>
          edge.distanceGrid.set(a, (i * 17 + j * 23) % 113)
          edge.distanceGrid.set(b, (i * 31 + j * 7) % 127)
        }
        val expected = edges.map(e => e.distanceGrid.getUnchecked(a) + e.distanceGrid.getUnchecked(b)).min
        assert(distances.exact(a, b) == expected)
      }
      assert(distances.exact(Tile(-1, 0), b) == distances.impossiblyLargeDistanceTiles)
      assert(distances.exact(a, Tile(With.mapTileWidth, 0)) == distances.impossiblyLargeDistanceTiles)
    } finally set(With.geography, "_zoneByTile", Array.fill(With.mapTileArea)(zone))
  }
  def armed(sim: Simulation, kind: UnitClass, at: Pixel, damage: Int): Simulacrum = {
    val s = mover(sim, at, kind, kind.isFlyer)
    s.isFriendlyCached = true
    s.player = player(false)
    set(s, "damageOnHitGroundCache", new Cache(() => damage))
    set(s, "damageOnHitAirCache", new Cache(() => damage))
    s
  }
  def splashVictim(sim: Simulation, x: Int, y: Int = 256, kind: UnitClass = Protoss.Dragoon): Simulacrum = {
    val s = mover(sim, Pixel(x, y), kind, kind.isFlyer)
    s.hitPoints = 500
    s.hitPointsInitial = 500
    s.player = player(true)
    s.valuePerDamage = 1
    s
  }
  check("collision off permits overlap and terrain crossing while retaining grid membership") {
    val sim = new Simulation(collisionEnabled = false)
    val moving = mover(sim, Pixel(60, 48))
    mover(sim, Pixel(80, 48))
    With.grids.walkableTerrain.set(Tile(2, 1), false)
    try {
      sim.grid.tryMove(moving, Pixel(176, 48))
      assert(moving.pixel == Pixel(64, 48))
      assert(sim.grid.tryForceV2(moving, Force(1, 0)))
      assert(!sim.grid.tryYield(moving))
      gridConsistent(sim)
    } finally java.util.Arrays.fill(With.grids.walkableTerrain.rawValues, true)
  }
  check("collision mode disables the choke modifier before inspecting choke geometry") {
    val previous = With.simulation
    With.simulation = new Simulation(collisionEnabled = true)
    try assert(JudgmentModifiers.choke(null).isEmpty) finally With.simulation = previous
  }
  check("siege splash uses inclusive footprint radii and hits primary exactly once") {
    val sim = simulation()
    val tank = armed(sim, Terran.SiegeTankSieged, Pixel(64, 256), 70)
    val primary = splashVictim(sim, 256)
    val distances = Seq(10, 11, 25, 26, 40, 41)
    val victims = distances.map(d => splashVictim(sim, 256 + Protoss.Dragoon.dimensionLeft + d))
    tank.dealDamageTo(primary)
    assert(primary.hitPoints == 430)
    assert(victims.map(_.hitPoints) == Seq(430, 465, 465, 482, 482, 500))
    assert(tank.cooldownLeft == tank.cooldownMaxAgainst(primary))
  }
  check("splash applies falloff before each victim's shields, armor and damage type") {
    val sim = simulation()
    val tank = armed(sim, Terran.SiegeTankSieged, Pixel(64, 256), 70)
    val primary = splashVictim(sim, 256)
    val marine = splashVictim(sim, 256 + Terran.Marine.dimensionLeft + 20, kind = Terran.Marine)
    marine.shieldPoints = 10
    marine.armorShield = 3
    marine.armorHealth = 2
    tank.dealDamageTo(primary)
    assert(marine.shieldPoints == 0 && marine.hitPoints == 490) // (70/2 - 3 - 10 - 2) * 0.5
  }
  check("radial splash damages allies without awarding kills, excludes shooter and respects target layers") {
    val sim = simulation()
    val tank = armed(sim, Terran.SiegeTankSieged, Pixel(256, 256), 70)
    val primary = splashVictim(sim, 256)
    val ally = splashVictim(sim, 258)
    ally.isFriendlyCached = true
    ally.hitPoints = 1
    val flyer = splashVictim(sim, 256, kind = Protoss.Scout)
    tank.dealDamageTo(primary)
    assert(tank.hitPoints == 100 && ally.hitPoints == 0 && tank.kills == 0)
    assert(flyer.hitPoints == 500 && tank.damageDealt == 69)
    ally.update()
    assert(ally.gridTile.isEmpty)
  }
  check("Archon enemy splash can cross air and ground layers but cannot hurt allies") {
    val sim = simulation()
    val archon = armed(sim, Protoss.Archon, Pixel(128, 256), 30)
    val primary = splashVictim(sim, 256)
    val ally = splashVictim(sim, 258)
    ally.isFriendlyCached = true
    val flyer = splashVictim(sim, 256, kind = Protoss.Scout)
    archon.dealDamageTo(primary)
    assert(ally.hitPoints == 500 && flyer.hitPoints == 470 && primary.hitPoints == 470)
  }
  check("air splash caps secondary inner damage at half and excludes ground and allied units") {
    val sim = simulation()
    val corsair = armed(sim, Protoss.Corsair, Pixel(128, 256), 8)
    val primary = splashVictim(sim, 256, kind = Protoss.Scout)
    val inner = splashVictim(sim, 256, kind = Protoss.Scout)
    val outer = splashVictim(sim, 256 + Protoss.Scout.dimensionLeft + 75, kind = Protoss.Scout)
    val ground = splashVictim(sim, 256)
    val ally = splashVictim(sim, 256, kind = Protoss.Scout)
    ally.isFriendlyCached = true
    corsair.dealDamageTo(primary)
    assert(primary.hitPoints == 492 && inner.hitPoints == 496 && outer.hitPoints == 498)
    assert(ground.hitPoints == 500 && ally.hitPoints == 500)
  }
  check("splash reaches large footprints beyond the center search radius and follows relocation") {
    val sim = simulation()
    val tank = armed(sim, Terran.SiegeTankSieged, Pixel(64, 256), 70)
    val primary = splashVictim(sim, 256)
    val building = splashVictim(sim, 256 + Terran.CommandCenter.dimensionLeft + 39, kind = Terran.CommandCenter)
    val moved = splashVictim(sim, 700)
    sim.grid.relocateUnit(moved, Pixel(260, 256))
    tank.dealDamageTo(primary)
    assert(building.hitPoints == 482 && moved.hitPoints == 430)
  }
  check("splash hits undetected units, ignores protected units, and limits burrowed hits to the inner radius") {
    val sim = simulation()
    val tank = armed(sim, Terran.SiegeTankSieged, Pixel(64, 256), 70)
    val primary = splashVictim(sim, 256)
    val hidden = splashVictim(sim, 256)
    hidden.cloaked = true
    hidden.detected = false
    val protectedUnit = splashVictim(sim, 256)
    protectedUnit.invincible = true
    val stasised = splashVictim(sim, 256)
    stasised.stasised = true
    val inner = splashVictim(sim, 256)
    inner.burrowed = true
    val outer = splashVictim(sim, 256 + Protoss.Dragoon.dimensionLeft + 20)
    outer.burrowed = true
    tank.dealDamageTo(primary)
    assert(hidden.hitPoints == 430 && inner.hitPoints == 430)
    assert(protectedUnit.hitPoints == 500 && stasised.hitPoints == 500 && outer.hitPoints == 500)
  }
  check("Reaver uses the Scarab weapon and upgraded primary damage") {
    val sim = simulation()
    val reaver = armed(sim, Protoss.Reaver, Pixel(64, 256), 125)
    val primary = splashVictim(sim, 256)
    val nearby = splashVictim(sim, 260)
    assert(reaver.damageVolleyAgainst(primary).weapon == WeaponType.Scarab)
    reaver.dealDamageTo(primary)
    assert(primary.hitPoints == 375 && nearby.hitPoints == 375)
    val upgraded = mover(sim, Pixel(64, 64), Protoss.Reaver)
    val upgrades = new HashMap[Upgrade, Cache[Int]]
    upgrades.put(Protoss.ScarabDamage, new Cache(() => 1))
    upgraded.player = player(false)
    set(upgraded.player, "upgradeLevelCaches", upgrades)
    assert(upgraded.damageOnHitGround == 125)
  }
  check("collision off and non-splash weapons retain single-target damage") {
    val disabled = new Simulation(collisionEnabled = false)
    disabled.battle = simulation().battle
    val tank = armed(disabled, Terran.SiegeTankSieged, Pixel(64, 256), 70)
    val primary = splashVictim(disabled, 256)
    val nearby = splashVictim(disabled, 260)
    tank.dealDamageTo(primary)
    assert(primary.hitPoints == 430 && nearby.hitPoints == 500)
    val sim = simulation()
    val devourer = armed(sim, Zerg.Devourer, Pixel(128, 256), 25)
    val airPrimary = splashVictim(sim, 256, kind = Protoss.Scout)
    val airNearby = splashVictim(sim, 260, kind = Protoss.Scout)
    devourer.dealDamageTo(airPrimary)
    assert(airPrimary.hitPoints == 475 && airNearby.hitPoints == 500)
  }
  check("Lurker splash follows the firing line, hits once, and excludes units beside or behind it") {
    val sim = simulation()
    val lurker = armed(sim, Zerg.Lurker, Pixel(128, 256), 20)
    val primary = splashVictim(sim, 220)
    val before = splashVictim(sim, 175)
    val beyond = splashVictim(sim, 290)
    val beside = splashVictim(sim, 220, 330)
    val behind = splashVictim(sim, 64)
    lurker.dealDamageTo(primary)
    assert(Seq(primary, before, beyond).forall(_.hitPoints == 480))
    assert(beside.hitPoints == 500 && behind.hitPoints == 500)
  }
  check("committed splash survives source death and strikes current neighbors at impact time") {
    val sim = simulation()
    val reaver = armed(sim, Protoss.Reaver, Pixel(64, 256), 100)
    val primary = splashVictim(sim, 256)
    val nearby = splashVictim(sim, 260)
    sim.realUnits ++= Seq(primary.realUnit, nearby.realUnit)
    With.frame = 1000
    primary.realUnit.addDamage(reaver, 2, true, false, Some(reaver.damageVolleyAgainst(primary)), Some(900))
    val reset = classOf[Simulation].getDeclaredMethods.find(_.getName.endsWith("resetPendingDamage")).get
    reset.setAccessible(true)
    reset.invoke(sim, Int.box(With.frame))
    reaver.alive = false
    sim.step()
    assert(primary.hitPoints == 500 && nearby.hitPoints == 500)
    sim.step()
    assert(primary.hitPoints == 400 && nearby.hitPoints == 400)
  }
  // Keep the physical battle fixed while independently changing combat ownership.
  class ForecastSquad extends Squad {
    var group: CombatGroup = CombatGroup.army
    override def combatGroup: CombatGroup = group
    override def run(): Unit = {}
    override def launch(): Unit = {}
  }
  def forecastSquad(group: CombatGroup): ForecastSquad = {
    val squad = allocate(classOf[ForecastSquad])
    squad.group = group
    squad
  }
  def forecastUnit(hp: Int, kind: UnitClass, enemy: Boolean = false): FriendlyUnitInfo = {
    val u = unit(hp)
    set(u, "friendly", Some(u))
    set(u, "_squad", None)
    set(u, "_unitClass", kind)
    set(u, "_player", player(enemy))
    u.simulacrum.unitClass = kind
    u.simulacrum.player = u.player
    u.simulacrum.isFriendlyCached = !enemy
    setWeapons(u.simulacrum)
    u
  }
  With.configuration.simulationAsynchronous = true
  val armyUnit = forecastUnit(40, Protoss.Dragoon)
  val dropUnit = forecastUnit(200, Protoss.Reaver)
  val transport = forecastUnit(80, Protoss.Shuttle)
  val defender = forecastUnit(100, Terran.Marine, enemy = true)
  val dropGroup = new CombatGroup("Drop")
  val dropSquad = forecastSquad(dropGroup)
  dropUnit.setSquad(Some(dropSquad))
  transport.setSquad(Some(dropSquad))
  def physical(ours: Seq[FriendlyUnitInfo]): Battle = new Battle(ours, Vector(defender), isGlobal = false)
  val physicalBattle = physical(Vector(armyUnit, dropUnit, transport))
  val forecasts = physicalBattle.combatForecasts
  With.battles.byUnit = physicalBattle.units.map(_ -> physicalBattle).toMap

  // Use real prediction steps, damage, checkpoints, and judgment scoring with synthetic BWAPI state.
  def predictForecast(battle: Battle): BattleJudgment = {
    val sim = new Simulation
    sim.battle = battle
    sim.shouldReset = false
    sim.realUnits ++= battle.units
    sim.realUnitsOurs ++= battle.us.units
    sim.realUnitsEnemy ++= battle.enemy.units
    sim.realUnits.foreach { u =>
      val s = u.simulacrum
      s.simulation = sim
      s.hitPoints = if (u == armyUnit) 40 else if (u == defender) 100 else 200
      s.hitPointsInitial = s.hitPoints
      s.alive = true
      s.damageDealt = 0
      s.damageReceived = 0
      s.valueDealt = 0
      s.valueReceived = 0
      s.kills = 0
      s.cooldownLeft = 0
      s.target = None
      s.threat = None
      s.gridTile = None
      s.events.clear()
      s.setPixel(Pixel(64, 64))
      sim.grid.populate(s)
      s.behavior = new SimulacrumBehavior {
        val fighting = true
        def act(actor: Simulacrum): Unit = {
          if (actor.alive && actor.unitClass != Protoss.Shuttle) {
            sim.simulacraEnemiesOf(actor).find(_.alive).foreach { target =>
              val damage = if (actor.unitClass == Protoss.Reaver) 100 else if (actor.isFriendlyCached) 5 else 20
              target.receiveDamage(DamageDealt(damage, 0))
            }
          }
        }
      }
    }
    while (!battle.simulationComplete) sim.step()
    val scoring = new Battle(isGlobal = false) {
      override lazy val skimWeight = 0.0
      override lazy val judgmentModifiers = Vector.empty
    }
    scoring.simulationCheckpoints ++= battle.simulationCheckpoints
    val judgment = new BattleJudgment(scoring)
    battle.judgement = Some(judgment)
    judgment
  }
  check("one spatial battle gives army and drop disjoint forecasts against the same defenders") {
    assert(armyUnit.battle.contains(physicalBattle) && dropUnit.battle.contains(physicalBattle))
    assert(armyUnit.team.contains(physicalBattle.us) && dropUnit.team.contains(physicalBattle.us))
    assert(forecasts.forecasts.size == 2)
    assert(armyUnit.combatForecast.get.us.units == Vector(armyUnit))
    assert(dropUnit.combatForecast == transport.combatForecast)
    assert(dropUnit.combatForecast.get.us.units.toSet == Set(dropUnit, transport))
    assert(forecasts.forecasts.forall(_.battle.enemy.units == Vector(defender)))
    assert(armyUnit.combatAllies.isEmpty && !dropUnit.combatAllies.exists(_ == armyUnit))
    assert(physicalBattle.judgement.isEmpty)
  }
  check("army can retreat while a drop in the same cluster wins its own forecast") {
    val army = predictForecast(armyUnit.combatForecast.get)
    val armyScore = army.scoreTotal
    val drop = predictForecast(dropUnit.combatForecast.get)
    assert(!army.shouldFightGround && drop.shouldFightGround)
    assert(armyUnit.combatJudgment.contains(army) && dropUnit.combatJudgment.contains(drop))
    near(army.scoreTotal, armyScore)
  }
  check("adding an independent drop does not improve the army prediction") {
    val alone = physical(Vector(armyUnit))
    val score = predictForecast(alone).scoreTotal
    near(score, armyUnit.combatJudgment.get.scoreTotal)
    assert(alone.combatForecasts.forecasts.head.battle eq alone)
  }
  check("ordinary squads share one forecast and moving a drop back invalidates old jobs") {
    dropUnit.setSquad(Some(forecastSquad(CombatGroup.army)))
    transport.setSquad(None)
    assert(armyUnit.combatForecast.isEmpty && dropUnit.combatForecast.isEmpty)
    assert(armyUnit.combatJudgment.isEmpty && armyUnit.combatAllies.isEmpty)
    near(armyUnit.confidence11, -1)
    val estimate = Micro.Actions.Combat.Decisionmaking.FightOrFlee.getClass.getDeclaredMethods.find(_.getName.endsWith("applyEstimation")).get
    estimate.setAccessible(true)
    estimate.invoke(Micro.Actions.Combat.Decisionmaking.FightOrFlee, armyUnit)
    assert(!armyUnit.agent.shouldFight && armyUnit.agent.fightReason == "AwaitForecast")
    val together = physical(Vector(armyUnit, dropUnit, transport))
    assert(together.combatForecasts.forecasts.size == 1)
    assert(together.combatForecasts.forecasts.head.battle eq together)
    assert(predictForecast(together).shouldFightGround)
  }
  check("recruiting a unit out of an army invalidates its previous combined prediction") {
    val parent = physical(Vector(armyUnit, dropUnit))
    assert(parent.combatForecasts.forUnit(armyUnit).contains(parent))
    dropUnit.setSquad(Some(dropSquad))
    assert(parent.combatForecasts.forUnit(armyUnit).isEmpty)
    assert(parent.combatForecasts.forUnit(dropUnit).isEmpty)
  }
  check("mission reuse and termination invalidate frozen ownership without changing squads") {
    val parent = physical(Vector(armyUnit, dropUnit))
    val before = parent.combatForecasts.forUnit(dropUnit)
    assert(before.isDefined)
    dropSquad.group = new CombatGroup("Drop relaunched")
    CombatGroup.invalidate()
    assert(parent.combatForecasts.forUnit(dropUnit).isEmpty)
    val relaunched = physical(Vector(armyUnit, dropUnit))
    assert(relaunched.combatForecasts.forUnit(dropUnit).isDefined)
    dropSquad.group = CombatGroup.army
    CombatGroup.invalidate()
    assert(relaunched.combatForecasts.forUnit(armyUnit).isEmpty)
  }
  check("report snapshots survive reuse of an enemy simulacrum by another forecast") {
    val s = defender.simulacrum
    s.alive = false
    s.damageReceived = 77
    s.setPixel(Pixel(128, 128))
    val report = new ReportCard(s, physicalBattle)
    s.alive = true
    s.damageReceived = 0
    s.setPixel(Pixel(256, 256))
    assert(!report.alive && report.damageReceived == 77 && report.pixel == Pixel(128, 128))
  }
  check("prediction queue contains separate jobs while the spatial index retains their parent") {
    dropUnit.setSquad(Some(dropSquad))
    dropSquad.group = new CombatGroup("Another drop")
    CombatGroup.invalidate()
    val parent = physical(Vector(armyUnit, dropUnit))
    With.battles.nextBattlesLocal = Vector(parent)
    val jobs = With.battles.nextBattles.take(2)
    assert(jobs.toSet == parent.combatForecasts.forecasts.map(_.battle).toSet)
    assert(!jobs.contains(parent) && With.battles.byUnit(armyUnit) == physicalBattle)
  }
  check("skim targets exclude another group's distraction even when it is the real enemy target") {
    import Information.Battles.Prediction.Skimulation.Skimulator
    import Performance.KeyedCache
    import ProxyBwapi.UnitInfo.UnitInfo
    set(defender, "_pixel", Pixel(400, 64))
    set(armyUnit, "_pixel", Pixel(64, 64))
    set(dropUnit, "_pixel", Pixel(368, 64))
    set(defender, "_presumptiveTargetCache", new KeyedCache[Option[UnitInfo], Int](() => Some(dropUnit), () => 0))
    val parent = physical(Vector(armyUnit, dropUnit))
    val army = parent.combatForecasts.forUnit(armyUnit).get
    val distance = Skimulator.getClass.getDeclaredMethods.find(_.getName.endsWith("distanceToEngage")).get
    distance.setAccessible(true)
    val actual = distance.invoke(Skimulator, defender, army.enemy).asInstanceOf[Double]
    near(actual, defender.pixelDistanceEdge(armyUnit))
    assert(actual > defender.pixelDistanceEdge(dropUnit))
    assert(defender.skimTarget.contains(armyUnit))
  }
  check("squad consensus drops cached fight permission immediately after ownership changes") {
    dropUnit.setSquad(None)
    val parent = physical(Vector(armyUnit, dropUnit))
    parent.combatForecasts
    val judgment = allocate(classOf[BattleJudgment])
    set(judgment, "shouldFightGround", true)
    parent.judgement = Some(judgment)
    With.battles.byUnit = parent.units.map(_ -> parent).toMap
    armyUnit.agent.shouldFight = true
    val squad = new Tactic.Squads.TFriendlyUnitGroup {
      def groupFriendlyUnits = Vector(armyUnit)
    }
    assert(squad.fightConsensus)
    dropUnit.setSquad(Some(dropSquad))
    assert(!squad.fightConsensus)
    near(squad.confidence11, -1)
  }
  println(s"$passed combat prediction checks passed")
}
