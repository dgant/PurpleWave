package Information.Battles.Prediction.Simulation

import Information.Battles.Battles
import Information.Battles.Prediction.SimulationCheckpoint
import Information.Battles.Types.{Battle, BattleJudgment, Hysteresis}
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Physics.Force
import Information.Geography.Geography
import Information.Geography.Pathfinding.Paths
import Information.Geography.Types.Zone
import Information.Grids.Grids
import Information.Grids.Movement.{GridWalkable, GridWalkableTerrain, GridUnwalkableUnits, GridFlowField}
import Planning.Blackboard
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.Players.{PlayerInfo, PlayerProxy}
import ProxyBwapi.Bullets.BulletInfo
import Micro.Coordination.DamageTracker
import Micro.Matchups.MatchupAnalysis
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

  def unit(hp: Int = 10, shields: Int = 0): FriendlyUnitInfo = {
    val u = allocate(classOf[FriendlyUnitInfo])
    set(u, "_pixel", Pixel(64, 64))
    set(u, "_player", With.self)
    set(u, "damageQueue", new ArrayBuffer[u.DamageSource])
    val s = new Simulacrum(u)
    set(u, "simulacrum", s)
    s.player = With.self
    s.unitClass = Protoss.Dragoon
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
    set(kiter, "pixelRangeGroundCache", new Cache(() => 192.0))
    set(kiter, "pixelRangeAirCache", new Cache(() => 192.0))
    set(kiter, "damageOnHitGroundCache", new Cache(() => 20))
    set(target, "pixelRangeGroundCache", new Cache(() => 32.0))
    kiter.behavior = BehaviorFight
    val start = kiter.pixel
    kiter.act()
    assert(kiter.moveGoal.exists(_.x > start.x))
    (1 to 20).foreach { _ => kiter.update(); kiter.act() }
    assert(target.hitPoints < 100, s"Next shot was delayed at distance ${kiter.pixelDistanceEdgeSimulation(target)}")
    gridConsistent(sim)
  }
  println(s"$passed combat prediction checks passed")
}
