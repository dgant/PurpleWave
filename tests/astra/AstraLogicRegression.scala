package Regression

import Debugging.Logger
import Information.Battles.Battles
import Information.Battles.Prediction.Simulation.{BehaviorFight, Simulacrum, Simulation}
import Information.Battles.Prediction.Skimulation.Skimulator
import Information.Battles.Types.{Battle, EnemyTeam, JudgmentModifiers}
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
  check("armor upgrades increase skim durability, including low-damage opponents") {
    for (damage <- Seq(6.0, 1.0, 0.0); kind <- Seq(Protoss.Dragoon, Terran.Goliath)) {
      def strength(armor: Int, shields: Int): Double = {
        val fighter = unit(kind, Pixel(160, 160), friendly = true)
        set(fighter, "_player", new PlayerInfo(HeadlessBWAPI.player(0, Race.Protoss)) {
          override def getUpgradeLevel(upgrade: ProxyBwapi.Upgrades.Upgrade): Int =
            if (upgrade == Protoss.Shields) shields else if (kind.armorUpgrade.contains(upgrade)) armor else 0
        })
        val battle = new Battle(Seq(fighter), isGlobal = true) {
          override lazy val enemy: EnemyTeam = new EnemyTeam(this, Seq.empty) {
            override def meanDamageOnHit: Double = damage
          }
        }
        fighter.skimPresence = 1.0
        Skimulator.measureUnitStrength(fighter, battle.us, battle)
        fighter.skimStrength
      }
      val base = strength(0, 0)
      val upgraded = strength(3, 0)
      val shielded = strength(0, 3)
      assert(base > 0 && upgraded >= base && shielded >= base)
      assert(!upgraded.isInfinity && !shielded.isInfinity)
      if (damage == 6.0) {
        assert(upgraded > base)
        if (kind.maxShields > 0) assert(shielded > base)
      }
    }
  }
  check("Medic engagement distance excludes the Medic itself") {
    val medic = unit(Terran.Medic, Pixel(160, 160), friendly = true)
    val marine = unit(Terran.Marine, Pixel(480, 160), friendly = true)
    val method = Skimulator.getClass.getDeclaredMethods.find(_.getName.endsWith("distanceToEngage")).get
    method.setAccessible(true)
    val battle = new Battle(Seq(medic, marine), isGlobal = true)
    val distance = method.invoke(Skimulator, medic, battle.us).asInstanceOf[Double]
    assert(medic.skimTarget.contains(marine) && distance == medic.pixelDistanceEdge(marine))
    val alone = new Battle(Seq(medic), isGlobal = true)
    method.invoke(Skimulator, medic, alone.us)
    assert(medic.skimTarget.isEmpty)
  }
  check("group counts refresh after membership and unit-type changes") {
    val marine = unit(Terran.Marine, Pixel(160, 160), friendly = true)
    var members = Seq.empty[UnitInfo]
    val group = new Tactic.Squads.UnitGroup { override def groupUnits: Seq[UnitInfo] = members }
    assert(group.count(Terran.Marine) == 0)
    members = Seq(marine)
    With.frame += 1
    assert(group.count(Terran.Marine) == 1 && group.has(Terran.Marine))
    set(marine, "_unitClass", Terran.Firebat)
    With.frame += 1
    assert(group.count(Terran.Marine) == 0 && group.count(Terran.Firebat) == 1)
    members = Seq.empty
    With.frame += 1
    assert(!group.has(Terran.Marine, Terran.Firebat))
  }
  check("Zerg building families recognize higher tiers, never lower tiers") {
    for (kind <- Seq(Zerg.Hatchery, Zerg.Lair, Zerg.Hive, Zerg.Spire, Zerg.GreaterSpire, Protoss.Nexus)) {
      assert(kind.isHatchlike == Seq(Zerg.Hatchery, Zerg.Lair, Zerg.Hive).contains(kind))
      assert(kind.isLairlike == Seq(Zerg.Lair, Zerg.Hive).contains(kind))
      assert(kind.isSpirelike == Seq(Zerg.Spire, Zerg.GreaterSpire).contains(kind))
    }
    assert(Macro.Scheduling.MacroProducer(Zerg.Lair, Zerg.Mutalisk).supplyUsePerFrame == Zerg.Mutalisk.supplyRequired400 / 342.0f)
    assert(Macro.Scheduling.MacroProducer(Zerg.Hive, Zerg.Ultralisk).supplyUsePerFrame == Zerg.Ultralisk.supplyRequired400 / 342.0f)
  }
  check("air-only attack squads retain flying targets and reject ground targets") {
    With.frame += 1
    With.yolo = new Planning.Yolo { override def active: Boolean = true }
    set(With.scouting, "enemyHomeCache", new Performance.Cache(() => Tile(1, 1)))
    val airborne = unit(Terran.CommandCenter, Pixel(480, 480)).asInstanceOf[ForeignUnitInfo]
    set(airborne, "_flying", true)
    val ground = unit(Terran.Marine, Pixel(180, 180)).asInstanceOf[ForeignUnitInfo]
    ground.matchups = new MatchupAnalysis(ground) { override def pixelsToThreatRange: Option[Double] = None }
    val buffer = get[UnorderedBuffer[ForeignUnitInfo]](With.units, "bufferEnemy")
    buffer.add(airborne)
    buffer.add(ground)
    val squad = new Tactic.Squads.SquadAttack {
      override def attacksAir: Boolean = true
      override def attacksGround: Boolean = false
      override def attackKeyDistanceTo(pixel: Pixel): Double = pixel.pixelDistance(Pixel(160, 160))
      override def centroidKey: Pixel = Pixel(160, 160)
      override def engagedUpon: Boolean = false
    }
    try {
      squad.chooseMode()
      assert(squad.vicinity == airborne.pixel)
      assert(squad.targets.exists(t => t.contains(airborne) && !t.contains(ground)))
    } finally { buffer.clear() }
  }
  check("supply-block diagnostics recognize exact-price and zero-gas affordability") {
    With.frame += 1
    With.configuration.debugging = true
    With.self = new PlayerInfo(HeadlessBWAPI.player(0, Race.Protoss)) {
      override def minerals: Int = Protoss.Zealot.mineralPrice
      override def gas: Int = 0
      override def supplyUsed400: Int = 18
      override def supplyTotal400: Int = 18
    }
    val gateway = unit(Protoss.Gateway, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    set(gateway, "_framesIdleConsecutive", 1)
    val buffer = get[UnorderedBuffer[FriendlyUnitInfo]](With.units, "bufferFriendly")
    buffer.add(gateway)
    val efficiency = new Information.Counting.Efficiency
    try {
      assert(gateway.framesIdleConsecutive > 0)
      efficiency.update()
      assert(efficiency.framesSupplyBlocked == 1 && efficiency.framesSupplyBlockedConsecutively == 1)
    } finally { buffer.clear(); With.self = us }
  }
  check("building clearance checks every tile in the footprint") {
    val terrain = Array.fill(With.mapTileWidth, With.mapTileHeight)(true)
    set(With.game, "mapWidth", With.mapTileWidth)
    set(With.game, "mapHeight", With.mapTileHeight)
    set(With.game, "buildable", terrain)
    terrain(5)(5) = false
    terrain(14)(17) = false
    terrain(24)(22) = false
    for ((w, h) <- Seq((1, 1), (2, 2), (3, 2), (4, 3), (6, 3))) {
      val grid = new Information.Grids.Construction.GridBuildableWH(w, h)
      grid.initialize()
      for (x <- 0 until With.mapTileWidth; y <- 0 until With.mapTileHeight) {
        val expected = x + w <= With.mapTileWidth && y + h <= With.mapTileHeight &&
          (x until x + w).forall(tx => (y until y + h).forall(ty => terrain(tx)(ty)))
        assert(grid.get(Tile(x, y)) == expected, s"$w x $h at ($x, $y)")
      }
    }
  }
  check("groups require detection against both cloaked and burrowed targets") {
    var detection = false
    val group = new Tactic.Squads.UnitGroup {
      override def groupUnits: Seq[UnitInfo] = Seq.empty
      override def attacksGround: Boolean = true
      override def detectionAvailable: Boolean = detection
    }
    val lurker = unit(Zerg.Lurker, Pixel(160, 160))
    set(lurker, "_burrowed", true)
    assert(group.canAttackIfVisible(lurker) && !group.canAttack(lurker))
    detection = true
    assert(group.canAttack(lurker))
    detection = false
    set(lurker, "_burrowed", false)
    assert(group.canAttack(lurker))
  }
  check("in-range combat targets take priority even when the closest target is passive") {
    With.frame += 1
    val attacker = unit(Protoss.Dragoon, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val passive = unit(Protoss.Pylon, Pixel(180, 160))
    val marine = unit(Terran.Marine, Pixel(220, 160))
    val distant = unit(Terran.Marine, Pixel(700, 160))
    attacker.intent.setTargets(Seq(distant))
    attacker.matchups = new MatchupAnalysis(attacker) {
      override def targets: Seq[UnitInfo] = Seq(passive, marine, distant)
    }
    assert(attacker.matchups.targetNearest.contains(passive))
    assert(Micro.Targeting.Target.defaultTargets(attacker)._2 == Seq(marine))
  }
  check("unseen Zerg building morphs can reach their inferred completion") {
    for (kind <- Seq(Zerg.Lair, Zerg.Hive, Zerg.SunkenColony, Zerg.SporeColony, Zerg.GreaterSpire)) {
      val morph = unit(kind, Pixel(160, 160))
      set(morph, "_complete", false)
      set(morph, "_morphing", true)
      set(morph, "_lastClassChange", With.frame)
      With.frame += kind.buildFrames + 1
      assert(morph.remainingCompletionFrames == 1) // Visible evidence wins over the estimate.
      set(morph, "_visibility", Visibility.InvisibleNearby)
      assert(morph.remainingCompletionFrames == 0)
    }
  }
  check("building footprint caches follow relocation without waiting five seconds") {
    val building = unit(Terran.Barracks, Pixel(160, 160), friendly = true)
    val before = building.tileArea
    val tilesBefore = building.tiles.toVector
    With.frame += 1
    val newTopLeft = Tile(15, 15)
    set(building, "_tileTopLeft", newTopLeft)
    assert(building.tileArea.startInclusive == newTopLeft)
    assert(building.tiles.toVector == building.unitClass.tileArea.add(newTopLeft).tiles.toVector)
    assert(building.tiles.toVector != tilesBefore && building.tileArea != before)
  }
  check("Scourge production consumes Larva and uses the morph lifecycle") {
    assert(Zerg.Scourge.buildUnitsSpent == Vector(Zerg.Larva))
    assert(Macro.Requests.RequestUnit(Zerg.Scourge).makeProduction(0).isInstanceOf[Tactic.Production.ProduceUnitMorphed])
  }
  check("Greater Spire production consumes its Spire and uses the morph lifecycle") {
    assert(Zerg.GreaterSpire.buildUnitsSpent == Vector(Zerg.Spire))
    assert(Macro.Requests.RequestUnit(Zerg.GreaterSpire).makeProduction(0).isInstanceOf[Tactic.Production.ProduceUnitMorphed])
    assert(Zerg.GreaterSpire.mineralValue == Zerg.GreaterSpire.mineralPrice + Zerg.Spire.mineralValue)
    assert(Zerg.GreaterSpire.gasValue == Zerg.GreaterSpire.gasPrice + Zerg.Spire.gasValue)
  }
  check("a distant threat cannot make a Carrier ignore a nearby threat") {
    With.frame += 1
    With.reaction = new Performance.ReactionTimes {
      override def sluggishness: Int = 1
      override def agencyMax: Int = 10
    }
    val carrier = unit(Protoss.Carrier, Pixel(200, 200), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val target = unit(Terran.Marine, Pixel(510, 200))
    set(target, "_orderTargetPixel", Some(target.pixel))
    val near = unit(Terran.Marine, Pixel(400, 200))
    val far = unit(Terran.Marine, Pixel(1000, 200))
    val interceptor = unit(Protoss.Interceptor, Pixel(400, 200), friendly = true)
    set(interceptor, "_order", ProxyBwapi.Orders.InterceptorAttack)
    set(carrier, "_interceptors", Vector(interceptor))
    set(carrier, "_orderTarget", Some(target))
    carrier.agent.shouldFight = true
    carrier.sleepUntil = With.frame + 1 // Inspect the decision without sending a BWAPI command.
    carrier.matchups = new MatchupAnalysis(carrier) {
      override def threats: Seq[UnitInfo] = Seq(near, far)
      override def pixelsEntangled: Double = 0
    }
    assert(carrier.pixelDistanceEdge(target) > 32 * 8 && carrier.pixelDistanceEdge(target) <= 32 * 9)
    val action = Micro.Actions.Protoss.BeCarrier
    val perform = action.getClass.getDeclaredMethod("perform", classOf[FriendlyUnitInfo])
    perform.setAccessible(true)
    perform.invoke(action, carrier)
    assert(!carrier.agent.actions.contains("CarrierChase") && !carrier.agent.actions.contains("CarrierAttack"))
    assert(carrier.agent.decision.pixel.contains(target.pixel.project(carrier.agent.defaultHome, 7.5 * 32)))
  }
  check("recovering Mutalisks clear stale targets when no safe target remains") {
    With.frame += 1
    set(With.geography, "_zoneByTile", new Array[Information.Geography.Types.Zone](With.mapTileArea))
    val base = new Information.Geography.Types.Base("Recovery", Tile(3, 3), Set(Tile(3, 3)))
    set(With.geography, "_bases", Vector(base))
    val mutalisk = unit(Zerg.Mutalisk, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val stale = unit(Terran.Marine, Pixel(180, 160))
    set(mutalisk, "_hitPoints", 50)
    mutalisk.agent.put(Micro.Actions.Zerg.BeMutalisk.MutaliskRecovering)
    mutalisk.agent.toAttack = Some(stale)
    mutalisk.matchups = new MatchupAnalysis(mutalisk) {
      override def framesOfSafety: Double = 100
      override def targets: Seq[UnitInfo] = Seq.empty
    }
    val action = Micro.Actions.Zerg.BeMutalisk
    val perform = action.getClass.getDeclaredMethod("perform", classOf[FriendlyUnitInfo])
    perform.setAccessible(true)
    try {
      perform.invoke(action, mutalisk)
      assert(mutalisk.agent.toAttack.isEmpty && !mutalisk.agent.shouldFight)
    } finally { set(With.geography, "_bases", Vector.empty) }
  }
  check("construction blockers remember the workers they previously identified") {
    val action = Micro.Actions.Scouting.BlockConstruction
    val scout = unit(Protoss.Probe, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val builder = unit(Terran.SCV, Pixel(220, 160))
    val destination = Tile(18, 18)
    val base = new Information.Geography.Types.Base("Expansion", destination, Set(destination))
    base.owner = foe
    val bases = Array.fill[Option[Information.Geography.Types.Base]](With.mapTileArea)(None)
    bases(destination.i) = Some(base)
    set(With.geography, "_baseByTile", bases)
    set(builder, "_order", ProxyBwapi.Orders.PlaceBuilding)
    set(builder, "_orderTargetPixel", Some(destination.center))
    scout.matchups = new MatchupAnalysis(scout) { override def targets: Seq[UnitInfo] = Seq(builder) }
    assert(action.blockableBuilders(scout).toVector == Vector(builder))
    assert(scout.agent.get[Iterable[UnitInfo]](action).exists(_.toVector == Vector(builder)))
    set(builder, "_order", ProxyBwapi.Orders.AttackUnit)
    assert(action.blockableBuilders(scout).toVector == Vector(builder))
    scout.matchups = new MatchupAnalysis(scout) { override def targets: Seq[UnitInfo] = Seq.empty }
    assert(action.blockableBuilders(scout).isEmpty)
    assert(scout.agent.get[Iterable[UnitInfo]](action).exists(_.isEmpty))
  }
  check("the four-Pool Extractor trick can start before it needs to cancel") {
    var used = 18
    var extractor = false
    val requested = ArrayBuffer.empty[ProxyBwapi.Buildable]
    val cancelled = ArrayBuffer.empty[ProxyBwapi.Buildable]
    val plan = new Gameplans.Zerg.ZvE.ZvE4Pool {
      override def supplyUsed400: Int = used
      override def supplyTotal400: Int = 18
      override def minerals: Int = 100
      override def have(filters: Utilities.UnitFilters.UnitFilter*): Boolean = filters.contains(Zerg.Larva) || (extractor && filters.contains(Zerg.Extractor))
      override def get(request: Macro.Requests.RequestBuildable): Unit = { requested += request.buildable }
      override def once(quantity: Int, kind: UnitClass): Unit = {}
      override def pump(kind: UnitClass, maximumTotal: Int, maximumConcurrently: Int): Unit = {}
      override def cancel(buildables: ProxyBwapi.Buildable*): Unit = { cancelled ++= buildables }
    }
    def run(): Unit = { requested.clear(); cancelled.clear(); plan.executeBuild() }
    run()
    assert(requested.contains(Zerg.Extractor) && cancelled.isEmpty)
    extractor = true
    used = 16 // Building the Extractor frees one supply; wait for that supply to be spent.
    run()
    assert(!requested.contains(Zerg.Extractor) && cancelled.isEmpty)
    used = 18
    run()
    assert(cancelled.contains(Zerg.Extractor) && !requested.contains(Zerg.Extractor))
    used = 14 // No trick needed after losing units.
    run()
    assert(cancelled.contains(Zerg.Extractor))
  }
  check("a placement query with no indexed foundations returns no candidates") {
    With.placement = new Placement.Placement
    assert(new Placement.Access.PlacementQuery().defaultFoundations.isEmpty)
  }
  check("interrupted drop assembly terminates once and stops processing") {
    var releases = 0
    With.recruiter = new Macro.Allocation.Recruiter {
      override def release(owner: Macro.Allocation.Prioritized): Unit = { releases += 1 }
    }
    val mission = new Tactic.Missions.MissionDrop {
      override protected def additionalFormationConditions: Boolean = true
      override protected def additionalItineraryConditions(base: Information.Geography.Types.Base): Boolean = false
      override protected def recruitablePassenger(unit: UnitInfo): Boolean = false
      override protected def recruit(): Unit = {}
    }
    val transport = unit(Protoss.Shuttle, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val passenger = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    transport.setSquad(Some(mission))
    passenger.setSquad(Some(mission))
    mission.transports += transport
    mission.passengers += passenger
    mission.run()
    assert(releases == 1) // Continuing would terminate again for the empty itinerary.
    assert(transport.client == None && passenger.client == None)
  }
  check("evacuating drops skip their current base, not every remaining destination") {
    val previousHeight = With.mapTileHeight
    val previousPixelHeight = With.mapPixelHeight
    With.mapTileHeight = 128 // Put this base on the map perimeter, outside the central-base exclusion.
    With.mapPixelHeight = 4096
    try {
      val mission = new Tactic.Missions.MissionDrop {
        override protected def additionalFormationConditions: Boolean = true
        override protected def recruit(): Unit = {}
      }
      val destination = new Information.Geography.Types.Base("Next drop", Tile(3, 3), Set(Tile(3, 3)))
      destination.owner = foe
      mission.state = mission.Evacuating
      val method = classOf[Tactic.Missions.MissionDrop].getDeclaredMethods.find(_.getName.endsWith("skipBase")).get
      method.setAccessible(true)
      assert(!method.invoke(mission, destination).asInstanceOf[Boolean])
      val passenger = unit(Protoss.Zealot, Tile(3, 3).center, friendly = true).asInstanceOf[FriendlyUnitInfo]
      val bases = Array.fill[Option[Information.Geography.Types.Base]](With.mapTileArea)(None)
      bases(passenger.tile.i) = Some(destination)
      set(With.geography, "_baseByTile", bases)
      get[ArrayBuffer[FriendlyUnitInfo]](mission, "_unitsNow") += passenger
      assert(method.invoke(mission, destination).asInstanceOf[Boolean])
    } finally {
      With.mapTileHeight = previousHeight
      With.mapPixelHeight = previousPixelHeight
    }
  }
  check("scout-ejector eligibility remains safe after its target disappears") {
    With.frame = 1000
    With.geography = new Geography
    set(With.geography, "_zoneByTile", new Array[Information.Geography.Types.Zone](With.mapTileArea))
    With.scouting = new Information.Scouting.Scouting
    With.priorities = new Macro.Allocation.Priorities
    With.recruiter = new Macro.Allocation.Recruiter {
      override def satisfy(lock: Planning.ResourceLocks.LockUnits): Unit = {} // Inspect the installed predicate without acquiring units.
    }
    val scout = unit(Zerg.Overlord, Pixel(176, 160)).asInstanceOf[ForeignUnitInfo]
    val dragoon = unit(Protoss.Dragoon, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    val zealot = unit(Protoss.Zealot, Pixel(160, 160), friendly = true).asInstanceOf[FriendlyUnitInfo]
    var present = true
    set(With.scouting, "enemyScouts", new Performance.Cache(() => if (present) Vector(scout) else Vector.empty))
    set(With.scouting, "zonesToLookForEnemyScouts", new Performance.Cache(() => Vector.empty[Information.Geography.Types.Zone]))
    val tactic = new Tactic.Tactics.TacticEjectScout
    set(tactic, "targetScout", new Performance.Cache(() => if (present) Some(scout) else None))
    tactic.launch()
    assert(tactic.lock.matcher(dragoon))
    assert(!tactic.lock.matcher(zealot))
    present = false
    With.frame += 1
    assert(!tactic.lock.matcher(dragoon)) // Recruiter can re-check the old predicate before the next launch.
    tactic.launch()
    assert(!tactic.lock.matcher(dragoon))
  }
  println(s"$passed passed; ${failed.size} failed")
  assert(failed.isEmpty, failed.mkString(", "))
}
