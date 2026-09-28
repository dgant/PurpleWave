package Regression

import Debugging.Logger
import Information.Battles.Battles
import Information.Battles.Prediction.Simulation.{BehaviorFight, Simulacrum, Simulation}
import Information.Battles.Types.{Battle, JudgmentModifiers}
import Information.Fingerprinting.{Fingerprint, Fingerprints}
import Information.Fingerprinting.Generic._
import Information.Fingerprinting.ProtossStrategies._
import Information.Fingerprinting.ZergStrategies._
import Information.GameSense.EconomicModel
import Information.Geography.Geography
import Information.Geography.Pathfinding.PathfindProfile
import Information.Grids.Grids
import Lifecycle.With
import Lifecycle.Configure.Configuration
import Mathematics.Points.{Pixel, PixelRectangle, Point, Tile}
import Mathematics.Shapes.Spiral
import Micro.Heuristics.SpellTargetAOE
import Micro.Actions.Basic.Cancel
import Micro.Matchups.{MatchupAnalysis, MatchupGraph}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{BWAPICachedUnitProxy, ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.{UnitTracker, UnorderedBuffer, Visibility}
import Utilities.Time.{Frames, GameTime, Seconds}
import bwapi.{HeadlessBWAPI, Race}

import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

object AstraLogicRegression extends App {
  var passed = 0
  val failed = ArrayBuffer.empty[String]
  def check(name: String)(body: => Unit): Unit = {
    try { body; passed += 1; println("PASS " + name) }
    catch { case error: Throwable => failed += name; println("FAIL " + name); error.printStackTrace() }
  }

  // Populate live proxy snapshots without calling readProxy (which requires a running BWAPI client).
  def field(target: AnyRef, name: String): java.lang.reflect.Field = {
    val classes = Iterator.iterate[Class[_]](target.getClass)(_.getSuperclass).takeWhile(_ != null)
    val result = classes.flatMap(_.getDeclaredFields).find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    result.setAccessible(true)
    result
  }
  def set(target: AnyRef, name: String, value: Any): Unit = field(target, name).set(target, value.asInstanceOf[AnyRef])
  def get[A](target: AnyRef, name: String): A = field(target, name).get(target).asInstanceOf[A]

  With.configuration = new Configuration
  With.logger = new Logger
  With.performance = new Performance.PerformanceMonitor
  With.mapTileWidth = 32
  With.mapTileHeight = 32
  With.mapTileArea = 1024
  With.mapPixelWidth = 1024
  With.mapPixelHeight = 1024
  With.mapWalkWidth = 128
  With.mapWalkHeight = 128
  With.mapWalkArea = 16384
  val us = PlayerInfo(HeadlessBWAPI.player(0, Race.Protoss))
  val foe = PlayerInfo(HeadlessBWAPI.player(1, Race.Zerg))
  With.self = us
  With.enemies = Vector(foe)
  With.friendlies = Vector(us)
  With.game = HeadlessBWAPI.game(us.bwapiPlayer)
  With.proxy = new ProxyBwapi.ProxyBWAPI
  With.grids = new Grids
  With.units = new UnitTracker
  With.geography = new Geography
  With.matchups = new MatchupGraph
  With.battles = new Battles
  With.fingerprints = new Fingerprints
  var nextId = 0
  def unit(kind: UnitClass, pixel: Pixel, friendly: Boolean = false): BWAPICachedUnitProxy = {
    nextId += 1
    val raw = HeadlessBWAPI.unit(With.game, nextId, kind.base, pixel.x, pixel.y)
    val result = if (friendly) new FriendlyUnitInfo(raw, nextId) else new ForeignUnitInfo(raw, nextId)
    Seq[(String, Any)](
      "_player" -> (if (friendly) us else foe), "_unitClass" -> kind,
      "_pixel" -> pixel, "_pixelObserved" -> pixel, "_tile" -> pixel.tile,
      "_tileTopLeft" -> pixel.subtract(16 * kind.tileWidth, 16 * kind.tileHeight).tile,
      "_visibility" -> Visibility.Visible, "_alive" -> true, "_complete" -> true,
      "_powered" -> true, "_detected" -> true, "_flying" -> kind.isFlyer,
      "_hitPoints" -> kind.maxHitPoints, "_shieldPoints" -> kind.maxShields,
      "_lastSeen" -> With.frame).foreach { case (name, value) => set(result, name, value) }
    result.completionFrame = 0
    result
  }

  check("spiral radius zero contains only the origin") {
    assert(Spiral(0).toVector == Vector(Point(0, 0)))
  }
  check("spiral visits each tile exactly once and stays inside its radius") {
    for (radius <- 0 to 12) {
      val points = Spiral(radius).map(p => (p.x, p.y)).toVector
      val expected = (for (x <- -radius to radius; y <- -radius to radius) yield (x, y)).toSet
      assert(points.size == expected.size && points.toSet == expected)
    }
  }
  check("spiral supports partial, repeated, and interleaved traversals") {
    val spiral = Spiral(4)
    val first = spiral.iterator
    val second = spiral.iterator
    assert(first.take(7).toVector == second.take(7).toVector)
    val expected = spiral.toVector
    assert(spiral.toVector == expected)
    assert(spiral.filter(_.x >= 0).toVector == expected.filter(_.x >= 0))
  }
  check("rectangle nearestPoint stays correct on all sides (unchanged control)") {
    val rectangle = PixelRectangle(Pixel(10, 20), Pixel(30, 40))
    for (x <- 0 to 50; y <- 0 to 50) {
      assert(rectangle.nearestPoint(Pixel(x, y)) == Pixel(math.max(10, math.min(29, x)), math.max(20, math.min(39, y))))
    }
  }
  check("unitless flying and ground paths use the corresponding vulnerability") {
    val air = new PathfindProfile(Tile(5, 5), canCrossUnwalkable = Some(true))
    val ground = new PathfindProfile(Tile(5, 5), canCrossUnwalkable = Some(false))
    set(air, "finalized", true)
    set(ground, "finalized", true)
    assert(air.vulnerableByAir && !air.vulnerableByGround)
    assert(!ground.vulnerableByAir && ground.vulnerableByGround)
    assert(air.threatGrid eq With.grids.enemyRangeAir)
    assert(ground.threatGrid eq With.grids.enemyRangeGround)
  }
  check("two-Gate stickiness follows the game clock, not construction time") {
    With.frame = 0
    val normal = new Fingerprint2Gate
    val nineNine = new Fingerprint2Gate99
    assert(!normal.sticky && !nineNine.sticky)
    With.frame = GameTime(4, 31)()
    assert(normal.sticky && nineNine.sticky)
  }
  check("Random opponents use the discovered race for fingerprints") {
    val randomZerg = new PlayerInfo(HeadlessBWAPI.player(2, Race.Random)) {
      override def raceCurrent: Race = Race.Zerg
    }
    With.enemies = Vector(randomZerg)
    try {
      assert(new FingerprintRace(Race.Zerg).investigate)
      assert(!new FingerprintRace(Race.Protoss).investigate)
    } finally { With.enemies = Vector(foe) }
  }
  check("gas-empty evidence never forgets a previously seen early gas") {
    val gas = unit(Zerg.Extractor, Pixel(128, 128))
    var matching = Seq.empty[UnitInfo]
    val fingerprint = new FingerprintGasEmptyUntil(Frames(100)) {
      override protected def matchingGas: Iterable[UnitInfo] = matching
      def probe(): Boolean = investigate
    }
    assert(fingerprint.probe())
    matching = Seq(gas)
    assert(!fingerprint.probe())
    matching = Seq.empty // lastSeen has advanced beyond the cutoff, removing it from matchingGas
    assert(!fingerprint.probe())
  }
  check("Spire cutoffs leave one Mutalisk production duration") {
    val one = new Fingerprint1HatchMuta
    val two = new Fingerprint2HatchMuta
    val oneSpire = one.children(1).asInstanceOf[FingerprintOr].children.head
    val twoSpire = two.children(1).asInstanceOf[FingerprintOr].children.head
    assert(get[Int](oneSpire, "endFrame") + Zerg.Mutalisk.buildFrames == ZergTimings.TwoHatchMuta_MutaCompleteBy() - Seconds(5)())
    assert(get[Int](twoSpire, "endFrame") + Zerg.Mutalisk.buildFrames == ZergTimings.ThreeHatchMuta_MutaCompleteBy() - Seconds(5)())
  }
  check("two-Hatch Muta accepts each independent proof") {
    val fingerprint = new Fingerprint2HatchMuta
    assert(fingerprint.children.size == 2)
    set(fingerprint.children.head, "matched", true)
    val alternatives = fingerprint.children(1).asInstanceOf[FingerprintOr]
    assert(alternatives.children.size == 3)
    for (proof <- alternatives.children) {
      alternatives.children.foreach(set(_, "matched", false))
      set(proof, "matched", true)
      set(alternatives, "matched", alternatives.investigate)
      assert(fingerprint.investigate)
    }
  }
  check("nine-Pool/overpool expansion proofs require a second Hatchery") {
    With.frame = 0
    val buffer = get[UnorderedBuffer[ForeignUnitInfo]](With.units, "bufferEnemy")
    buffer.clear()
    buffer.add(unit(Zerg.Hatchery, Pixel(128, 128)).asInstanceOf[ForeignUnitInfo])
    val nine = new Fingerprint9PoolHatch
    val over = new FingerprintOverpoolHatch
    val proofs = Seq(nine, over).map(_.children.last.asInstanceOf[FingerprintOr].children.head.asInstanceOf[FingerprintCompleteBy])
    try {
      assert(proofs.forall(!_.investigate))
      buffer.add(unit(Zerg.Hatchery, Pixel(320, 320)).asInstanceOf[ForeignUnitInfo])
      assert(proofs.forall(_.investigate))
    } finally { buffer.clear() }
  }
  check("fight/retreat hysteresis records transitions only once") {
    val friendly = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    With.frame = 100
    friendly.agent.shouldFight = false
    friendly.hysteresis.update()
    With.frame = 125
    friendly.hysteresis.update()
    assert(friendly.hysteresis.decisionFrames == 25)
    friendly.agent.shouldFight = true
    friendly.hysteresis.update()
    assert(friendly.hysteresis.decisionFrames == 0 && friendly.hysteresis.lastShouldFight)
    With.frame = 150
    friendly.hysteresis.update()
    assert(friendly.hysteresis.decisionFrames == 25)
  }
  check("cancelling a unit class cancels that class's active trainee") {
    With.blackboard = new Planning.Blackboard
    val trainer = unit(Protoss.Gateway, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val trainee = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    set(trainer, "_training", true)
    set(trainee, "_complete", false)
    val buffer = get[UnorderedBuffer[FriendlyUnitInfo]](With.units, "bufferFriendly")
    buffer.add(trainee)
    With.frame += Seconds(6)()
    With.blackboard.toCancel.set(Vector(Protoss.Zealot))
    try { assert(Cancel.allowed(trainer)) }
    finally { buffer.clear(); With.blackboard.toCancel.reset() }
  }
  def spellScene(anchor: Pixel, other: Pixel, stale: Double, radius: Double, value: UnitInfo => Double): Option[Pixel] = {
    With.grids = new Grids
    val caster = unit(Protoss.HighTemplar, Pixel(120, 140), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val first = unit(Terran.Marine, anchor)
    val last = unit(Terran.Marine, other)
    last.spellTargetValue = stale
    caster.matchups = new MatchupAnalysis(caster) { override def allUnits: Seq[UnitInfo] = Seq(first, last, caster) }
    Seq(first, last, caster).foreach(With.grids.units.updateUnit)
    new SpellTargetAOE().chooseTargetPixel(caster, radius, 1.5, (u, _) => if (u == caster) 0 else value(u), 96, 96)
  }
  check("AOE scoring includes the final intersecting tile row and column") {
    assert(spellScene(Pixel(140, 140), Pixel(225, 225), 1, 500, _ => 1).nonEmpty)
  }
  check("AOE evaluates edge occupants outside the candidate search radius") {
    assert(spellScene(Pixel(140, 140), Pixel(185, 180), -100, 32, _ => 1).nonEmpty)
  }
  check("AOE does not inherit a stale positive score for harmful occupants") {
    assert(spellScene(Pixel(140, 140), Pixel(185, 180), 10, 32, u => if (u.pixel.x < 160) 1 else -10).isEmpty)
  }
  check("battle entrants are classified relative to both teams") {
    val friendly = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val enemy = unit(Zerg.Zergling, Pixel(180, 160))
    val friendlyEntrant = unit(Protoss.Dragoon, Pixel(160, 190), friendly = true)
    val enemyEntrant = unit(Zerg.Hydralisk, Pixel(180, 190))
    val battle = new Battle(Seq(friendly), Seq(enemy), isGlobal = true)
    With.battles.byUnit = Map(friendly -> battle, enemy -> battle)
    With.matchups.entrants(battle) = ArrayBuffer(friendlyEntrant, enemyEntrant)
    assert(enemy.matchups.alliesInclSelf.toSet == Set(enemy, enemyEntrant))
    assert(enemy.matchups.enemies.toSet == Set(friendly, friendlyEntrant))
    assert(friendly.matchups.alliesInclSelf.toSet == Set(friendly, friendlyEntrant))
    assert(friendly.matchups.enemies.toSet == Set(enemy, enemyEntrant))
  }
  check("minimum-range threats exclude a point-blank Siege Tank") {
    val zealot = unit(Protoss.Zealot, Pixel(160, 160), friendly = true)
    val tank = unit(Terran.SiegeTankSieged, Pixel(180, 160))
    val matchups = new MatchupAnalysis(zealot) { override def threats: Seq[UnitInfo] = Seq(tank) }
    assert(matchups.threatsInRange.isEmpty)
  }
  check("nearest in-range target is selected from all eligible targets") {
    val tank = unit(Terran.SiegeTankSieged, Pixel(160, 160))
    val near = unit(Protoss.Zealot, Pixel(180, 160), friendly = true)
    val far = unit(Protoss.Dragoon, Pixel(400, 160), friendly = true)
    val matchups = new MatchupAnalysis(tank) { override def targets: Seq[UnitInfo] = Seq(near, far) }
    assert(matchups.targetNearest.contains(near))
    assert(matchups.targetNearestInRange.contains(far))
  }
  check("simulation cannot fire at a newly selected point-blank target") {
    val simulation = new Simulation
    simulation.battle = new Battle(isGlobal = true)
    def sim(kind: UnitClass, x: Int): Simulacrum = {
      val result = new Simulacrum(null)
      result.simulation = simulation
      result.player = us
      result.isFriendlyCached = true
      result.unitClass = kind
      result.pixel = Pixel(x, 160)
      result.pixelLeft = x - kind.dimensionLeft
      result.pixelRightExclusive = x + kind.dimensionRightExclusive
      result.pixelTop = 160 - kind.dimensionUp
      result.pixelBottomExclusive = 160 + kind.dimensionDownExclusive
      result.hitPoints = kind.maxHitPoints
      result.shieldPoints = kind.maxShields
      result.alive = true
      result.detected = true
      result.unitSize = kind.size
      result
    }
    val tank = sim(Terran.SiegeTankSieged, 160)
    val target = sim(Protoss.Zealot, 180)
    tank.targets.add(target)
    val initialHealth = target.totalHealth
    BehaviorFight.act(tank)
    assert(tank.target.contains(target))
    assert(target.totalHealth == initialHealth && !simulation.engaged)
  }
  check("enemy upgrades count even when we own none of those upgrades") {
    With.scouting = new Information.Scouting.Scouting
    With.strategy = new Strategery.Strategist
    With.accounting = new Information.Counting.Accounting
    val upgradedEnemy = new PlayerInfo(HeadlessBWAPI.player(3, Race.Zerg)) {
      override def getUpgradeLevel(upgrade: ProxyBwapi.Upgrades.Upgrade): Int = if (upgrade == Zerg.ZerglingSpeed) 1 else 0
    }
    With.enemies = Vector(upgradedEnemy)
    val economy = new EconomicModel { def update(): Unit = updateEconomicModel() }
    try {
      economy.update()
      assert(economy.ourUpgradeMinerals == 0 && economy.ourUpgradeGas == 0)
      assert(economy.enemyUpgradeMinerals == Zerg.ZerglingSpeed.mineralPrice(1))
      assert(economy.enemyUpgradeGas == Zerg.ZerglingSpeed.gasPrice(1))
    } finally { With.enemies = Vector(foe) }
  }
  check("fight commitment decays fractionally between five/ten-second boundaries") {
    With.frame = 2000
    val fighter = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    fighter.matchups = new MatchupAnalysis(fighter) {
      override def engagedUpon: Boolean = true
      override def pixelsEntangled: Double = 0
      override def framesToLive: Double = 120
      override def threatDeepest: Option[UnitInfo] = None
      override def targetNearest: Option[UnitInfo] = None
      override def inTankRange: Boolean = false
      override def inRangeOfTank: Boolean = false
    }
    fighter.hysteresis.lastChangeFrame = With.frame - Seconds(5)()
    assert(math.abs(JudgmentModifiers.hysteresis(fighter)._1 + 0.075) < 0.000001)
    fighter.hysteresis.lastChangeFrame = With.frame
    fighter.lastFrameTakingDamage = With.frame - Seconds(5)() / 2
    fighter.damageQueue += fighter.DamageSource(fighter, With.frame + 1, committed = true, guaranteed = true, 1)
    assert(math.abs(JudgmentModifiers.hysteresis(fighter)._1 + 0.075) < 0.000001)
  }
  println(s"$passed passed; ${failed.size} failed")
  assert(failed.isEmpty, failed.mkString(", "))
}
