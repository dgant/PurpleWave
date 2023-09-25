package ProxyBwapi.UnitInfo

import Debugging.Visualizations.Colors
import Information.Battles.Prediction.Simulation.{ReportCard, Simulacrum}
import Information.Battles.Prediction.Skimulation.SkimulationUnit
import Information.Battles.Types.{Battle, Team}
import Information.Geography.Types.{Base, Metro, Zone}
import Lifecycle.With
import Macro.Allocation.Prioritized
import Mathematics.Maff
import Mathematics.Physics.Force
import Mathematics.Points._
import Mathematics.Shapes.Arc
import Micro.Coordination.Pathing.MicroPathing
import Micro.Matchups.MatchupAnalysis
import Micro.Targeting.TargetScoring
import Performance.{Cache, KeyedCache}
import ProxyBwapi.Orders
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade
import Utilities.?
import Utilities.Time.{Forever, Frames, Seconds}
import Utilities.UnitFilters.{IsHatchlike, UnitFilter}
import bwapi.Color

abstract class UnitInfo(val bwapiUnit: bwapi.Unit, val id: Int) extends UnitProxy with CombatUnit with SkimulationUnit with Targeted with UnitFilter {

  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None

  @inline final override val hashCode: Int = Math.abs(id + With.frame * 10000) // Abs in case of overflow, which can happen in very long games
  @inline final override def toString: String = f"${if (isFriendly) "Our" else if (isEnemy) "Foe" else "Neutral"} $unitClass${if (selected) "*" else ""}${if (complete) "" else f" ${Frames(With.frame + remainingCompletionFrames)}"} #$id $hitPoints/${unitClass.maxHitPoints} ${if (shieldPoints > 0) f"($shieldPoints/${unitClass.maxShields})" else ""} $pixel"

  @inline final def is(unitMatcher: UnitFilter): Boolean = unitMatcher(this)
  @inline final def isPrerequisite(unitMatcher: UnitFilter): Boolean = (
    unitMatcher(this)
      || unitMatcher == Zerg.Hatchery && isAny(Zerg.Lair, Zerg.Hive)
      || unitMatcher == Zerg.Lair && is(Zerg.Hatchery)
      || unitMatcher == Zerg.Spire && is(Zerg.GreaterSpire))
  @inline final def isNone(unitMatchers: UnitFilter*): Boolean = ! unitMatchers.exists(_(this))
  @inline final def isAny(unitMatchers: UnitFilter*): Boolean = unitMatchers.exists(_(this))
  @inline final def isAll(unitMatchers: UnitFilter*): Boolean = unitMatchers.forall(_(this))

  val frameDiscovered             : Int = With.frame
  val initialHitPoints            : Int = bwapiUnit.getHitPoints
  val initialShields              : Int = bwapiUnit.getShields
  var completionFrame             : Int = Forever() // Can't use unitClass during construction
  var lastShieldPoints            : Int = _
  var lastMatrixPoints            : Int = _
  var lastCooldown                : Int = _
  var lastFrameTakingDamage       : Int = - Forever()
  var lastFrameStartingAttack     : Int = - Forever()
  var lastFrameHarvested          : Int = - Forever()
  var lastFrameMiningGas          : Int = - Forever()
  var firstFrameMiningGas         : Int =   Forever()
  var hasEverBeenCompleteHatch    : Boolean = false // Stupid AIST hack fix for detecting whether a base is mineable
  var lastHitPoints               : Int = _
  @inline final def completionFrameFull: Int = completionFrame +  unitClass.framesToFinishCompletion
  private var lastUnitClass       : UnitClass = _
  private val previousPixels      : Array[Pixel] = Array.fill(48)(new Pixel(bwapiUnit.getPosition))
  @inline final def previousPixel(framesAgo: Int): Pixel = previousPixels((With.frame + previousPixels.length - Math.min(previousPixels.length, framesAgo)) % previousPixels.length)
  def update(): Unit = {
    if (visibleToOpponents) _frameKnownToOpponents = Math.min(_frameKnownToOpponents, With.frame)
    // We use cooldownGround/Air because for incomplete units cooldown is equal to remaining completion frames
    if (Math.max(cooldownGround, cooldownAir) > lastCooldown) {
      lastFrameStartingAttack = With.frame
    }
    if (totalHealth < lastHitPoints + lastShieldPoints + lastMatrixPoints) {
      lastFrameTakingDamage = With.frame
    }
    if (isEnemy && visible && unitClass.isWorker && orderTarget.exists(t => t.unitClass.isGas && t.player == player && t.complete)) {
      lastFrameMiningGas  = With.frame
      firstFrameMiningGas = Math.min(firstFrameMiningGas, With.frame)
    }
    if (complete) {
      // If the unit class changes (eg. Geyser -> Extractor) update the completion frame
      if (unitClass != lastUnitClass) completionFrame = With.frame
      // We may not know exactly when it finished; if so now is our best guess.
      // The most important consumer of this estimate is fingerprinting.
      // For fingerprinting, "finished now" is a pretty decent heuristic.
      completionFrame = Math.min(completionFrame, With.frame)
    } else {
      completionFrame = With.frame + remainingCompletionFrames
    }
    lastUnitClass     = unitClass
    lastHitPoints     = hitPoints
    lastShieldPoints  = shieldPoints
    lastMatrixPoints  = matrixPoints
    lastCooldown      = cooldownLeft
    hasEverBeenCompleteHatch ||= complete && is(IsHatchlike)
    previousPixels(With.frame % previousPixels.length) = pixel
  }

  @inline final def aliveAndComplete: Boolean = alive && complete
  @inline final def energyMax     : Int = unitClass.maxEnergy // TODO: Count effects of upgrades
  @inline final def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  @inline final def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0

  lazy val isBlocker: Boolean = (unitClass.isMinerals || unitClass.isGas) && gasLeft + mineralsLeft < 250 // Setting this goofily high as an AIIDE hack to account for the 249-mineral patches on Fortress
  lazy val inefficientGasPosition: Boolean = unitClass.isGas && base.map(_.townHallTile).exists(t => t.y + 3 < tileTopLeft.y || t.x + 4 < tileTopLeft.x)
  lazy val gasMinersRequired: Int = if (unitClass.isGas) (if (inefficientGasPosition) 4 else 3) else 0

  @inline final def subjectiveValue: Double = subjectiveValueCache()
  private val subjectiveValueCache = new Cache(() =>
    unitClass.subjectiveValue
      + ?(is(Protoss.Carrier), friendly.map(_.interceptorCount).getOrElse(8) * Protoss.Interceptor.subjectiveValue, 0)
      + ?(unitClass.isTransport, friendly.map(_.loadedUnits.map(_.subjectiveValue).sum).sum, 0))

  @inline final def remainingOccupationFrames: Int = Math.max(
    remainingCompletionFrames,  Math.max(
    remainingTechFrames,        Math.max(
    remainingUpgradeFrames,     Math.max(
    remainingTrainFrames,
    addon.map(_.remainingCompletionFrames).getOrElse(0)
  ))))
  @inline final def remainingFramesUntilMoving: Int = Math.max(
    remainingCompletionFrames,
    With.framesUntil(lastFrameStartingAttack + unitClass.stopFrames))

  private var _producer: Option[Prioritized] = None
  @inline final def setProducer(plan: Prioritized): Unit = { _producer = Some(plan) }
  @inline final def producer: Option[Prioritized] = _producer

  @inline final def exitTile  : Tile          = tileTopLeft.add(0, unitClass.tileHeight).walkableTile
  @inline final def addonArea : TileRectangle = TileRectangle(Tile(0, 0), Tile(2, 2)).add(tileTopLeft).add(4,1)
  @inline final def tiles     : Seq[Tile]     = cacheTiles() // TODO: Set this on type/pixel change
  @inline final def tileArea  : TileRectangle = cacheTileArea() // TODO: Set this on type/pixel change
  private def tileCacheDuration: Int = { if (unitClass.isBuilding) (if (unitClass.canFly) 24 * 5 else 24 * 60) else 1 }
  private lazy val cacheTileArea = new Cache(() => unitClass.tileArea.add(tileTopLeft), refreshPeriod = tileCacheDuration)
  private lazy val cacheTiles = new Cache(() => cacheTileArea().tiles.toVector, refreshPeriod = tileCacheDuration)

  @inline final def zone: Zone = if (unitClass.isBuilding) tileTopLeft.zone else tile.zone // Hack to get buildings categorized in zone they were intended to be constructed in
  @inline final def base: Option[Base] = tile.base
  @inline final def metro: Option[Metro] = tile.metro

  @inline final def canTraverse(pixel: Pixel): Boolean = pixel.traversableBy(this)
  @inline final def canTraverse(tile: Tile): Boolean = tile.traversableBy(this)

  @inline final def canMove: Boolean = canMoveCache()
  private val canMoveCache = new Cache(() =>
    (unitClass.canMove || (unitClass.isBuilding && flying))
    && unitClass.topSpeed > 0
    && canDoAnything
    && ! burrowed
    && ! Terran.SiegeTankSieged(this))
  @inline final def velocity: Force = Force(velocityX, velocityY)
  @inline final def topSpeed: Double = if (canMove) topSpeedPossibleCache() else 0
  @inline final def topSpeedPossible: Double = topSpeedPossibleCache()
  private val topSpeedPossibleCache = new Cache(() =>
      ?(ensnared, 0.5, 1.0) // TODO: Is this the multiplier?
    * ?(stimmed,  1.5, 1.0)
    * ?(is(Terran.SiegeTankSieged), Terran.SiegeTankUnsieged.topSpeed, unitClass.topSpeed)
    * ?(
        (is(Terran.Vulture)   && Terran.VultureSpeed    (player))  ||
        (is(Protoss.Observer) && Protoss.ObserverSpeed  (player)) ||
        (is(Protoss.Scout)    && Protoss.ScoutSpeed     (player)) ||
        (is(Protoss.Shuttle)  && Protoss.ShuttleSpeed   (player)) ||
        (is(Protoss.Zealot)   && Protoss.ZealotSpeed    (player)) ||
        (is(Zerg.Overlord)    && Zerg.OverlordSpeed     (player)) ||
        (is(Zerg.Zergling)    && Zerg.ZerglingSpeed     (player)) ||
        (is(Zerg.Hydralisk)   && Zerg.HydraliskSpeed    (player)) ||
        (is(Zerg.Ultralisk)   && Zerg.UltraliskSpeed    (player)),
      1.5, 1.0))
  @inline final def topSpeedTransported: Double = Math.max(topSpeed, friendly.flatMap(_.transport).map(_.topSpeed).getOrElse(topSpeed))

  @inline final def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tile, tiles)
  @inline final def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixel, pixels)

  @inline final def isTransport: Boolean = flying && ?(Zerg.Overlord(this), Zerg.OverlordDrops(player), unitClass.isTransport)

  @inline final def detectionRangePixels: Int = if (unitClass.isDetector) (if (unitClass.isBuilding) 32 * 7 else sightPixels) else 0
  @inline final def sightPixels: Int = sightRangePixelsCache()
  private val sightRangePixelsCache = new Cache(() =>
    if (blind) 32 else
    unitClass.sightPixels +
      (if (
        (is(Terran.Ghost)     && player.hasUpgrade(Terran.GhostVisionRange))      ||
        (is(Protoss.Observer) && player.hasUpgrade(Protoss.ObserverVisionRange))  ||
        (is(Protoss.Scout)    && player.hasUpgrade(Protoss.ScoutVisionRange))     ||
        (is(Zerg.Overlord)    && player.hasUpgrade(Zerg.OverlordVisionRange)))
      64 else 0))

  @inline final def formationRangePixels: Double = Math.max(
      0,
      ?(unitClass == Terran.SiegeTankUnsieged && Terran.SiegeMode(player), Terran.SiegeTankSieged.effectiveRangePixels, effectiveRangePixels)
    - ?(unitClass == Protoss.Reaver, ?(friendly.exists(_.agent.ride.isDefined), 64, 32), 0)
    - 32)

  @inline final def altitude: Double = tile.altitude

  @inline final def arrivalFrame: Int = _arrivalFrame()
  private val _arrivalFrame = new Cache(() => {
    val home        = With.geography.home.center
    val classSpeed  = unitClass.topSpeed
    val travelTime  = Math.min(
      24 * 60 * 60,
      if (canMove)              framesToTravelTo(home)
      else if (classSpeed > 0)  (pixelDistanceTravelling(home) / classSpeed).toInt
      else                      Int.MaxValue)
    val completionTime  = Maff.clamp(completionFrame, With.frame, With.frame + unitClass.buildFrames)
    val arrivalTime     = completionTime + travelTime
    arrivalTime
  })

  @inline final def proxied: Boolean = _proxied()
  private val _proxyThreshold = 0.42
  private val _proxied = new Cache(() =>
    unitClass.isBuilding
    && ! flying
    && ?(isFriendly,
      With.scouting.proximity(tile) < 1 - _proxyThreshold || ! metro.exists(_.bases.exists(_.isOurs)),
      With.scouting.proximity(tile) >     _proxyThreshold || ! metro.exists(_.bases.exists(_.isEnemy) || With.geography.enemyBases.isEmpty)),
    240)

  ////////////
  // Combat //
  ////////////

  @inline final def battle: Option[Battle] = With.battles.byUnit.get(this).orElse(With.matchups.entrants.find(_._2.contains(this)).map(_._1))
  @inline final def team: Option[Team] = battle.map(b => ?(isFriendly, b.us, b.enemy))
  @inline final def report: Option[ReportCard] = battle.flatMap(_.simulationReport.get(this))
  var matchups: MatchupAnalysis = MatchupAnalysis(this)
  val simulacrum: Simulacrum = new Simulacrum(this)
  var nextInCluster: Option[UnitInfo] = None

  var spellTargetValue: Double = _
  private val _injury = new Cache(() => _calculateInjury)
  private val _targetValue = new Cache(() => TargetScoring(this))
  def injury: Double = _injury()
  def targetValue: Double = _targetValue()

  @inline final def armorHealth: Int = armorHealthCache()
  @inline final def armorShield: Int = armorShieldsCache()
  private lazy val armorHealthCache   = new Cache(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  private lazy val armorShieldsCache  = new Cache(() => player.getUpgradeLevel(Protoss.Shields))

  @inline final def dpfAir    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir.toDouble
  @inline final def dpfGround : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround.toDouble

  private val _cooldownLeft: Cache[Int] = new Cache(() => {
    // Airlifted unit logic: https://github.com/OpenBW/openbw/blob/master/bwgame.h#L3165
    ?(
      Protoss.Reaver(this) && airlifted,
      30,
      Maff.vmax(
        remainingFramesUntilMoving,
        cooldownAir,
        cooldownGround,
        ?(airlifted, cooldownMaxAirGround, 0),
        friendly
          .filter(u => Protoss.Reaver(u) && u.scarabs == 0)
          .map(_.trainee.map(_.remainingCompletionFrames).getOrElse(Protoss.Scarab.buildFrames))
          .getOrElse(0)))
  })
  @inline final def cooldownLeft : Int = _cooldownLeft()

  @inline final def canDoAnything: Boolean = canDoAnythingCache()
  private val canDoAnythingCache = new Cache(() =>
    aliveAndComplete
    && ( ! unitClass.requiresPsi || powered)
    && ! stasised
    && ! maelstrommed
    && ! lockedDown)

  @inline final def canBeAttacked: Boolean = canBeAttackedCache()
  private val canBeAttackedCache = new Cache(() => alive
    && (complete || unitClass.isBuilding)
    && totalHealth > 0
    && ! invincible
    && ! stasised)

  @inline final def canAttack: Boolean = canAttackCache()
  private val canAttackCache = new Cache(() =>
    (unitClass.rawCanAttack || (is(Terran.Bunker) && (isEnemy || friendly.exists(_.loadedUnits.exists(_.canAttack)))))
    && (flying || ! underDisruptionWeb)
    && canDoAnything)

  @inline final def canAttack(enemy: CombatUnit): Boolean = (
    canAttack
    && ! enemy.unitClass.isSpell
    && enemy.asInstanceOf[UnitInfo].canBeAttacked
    && (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
    && ! enemy.asInstanceOf[UnitInfo].effectivelyCloaked
    && (enemy.unitClass.triggersSpiderMines || ! Terran.SpiderMine(this))
    && ( ! unitClass.affectedByDarkSwarm || ! enemy.asInstanceOf[UnitInfo].underDarkSwarm))

  @inline final def canAttackAir    : Boolean = canAttack && attacksAgainstAir    > 0
  @inline final def canAttackGround : Boolean = canAttack && attacksAgainstGround > 0
  @inline final def canBurrow       : Boolean = canDoAnything && (is(Zerg.Lurker) || (Zerg.Burrow(player) && unitClass.canBurrow))

  def confidence11: Double = _confidence()
  private val _confidence = new Cache(() => if (matchups.threats.isEmpty) 1.0 else battle.flatMap(_.judgement.map(j => if (flying) j.confidence11Air else j.confidence11Ground)).getOrElse(1.0))

  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  @inline final def framesToBeReadyForAttackOrder: Int = cooldownLeft - With.latency.remainingFrames - With.reaction.agencyMin
  @inline final def readyForAttackOrder: Boolean = canAttack && framesToBeReadyForAttackOrder <= 0
  @inline final def framesToFace(target: UnitInfo): Int = framesToFace(target.pixel)
  @inline final def framesToFace(target: Pixel): Int = framesToFace(pixel.radiansTo(target))
  @inline final def framesToFace(faceRadians: Double): Int = unitClass.framesToTurn(Maff.radiansTo(angleRadians, faceRadians))
  @inline final def pixelsOfEntanglement(threat: UnitInfo): Double = {
    val speedTowardsThreat    = speedApproaching(threat)
    val framesToStopMe        = ?(speedTowardsThreat <= 0, 0.0, framesToStopRightNow)
    val framesToFlee          = framesToStopMe + unitClass.framesToTurn180 + With.latency.remainingFrames + With.reaction.agencyAverage
    val distanceClosedByMe    = speedTowardsThreat * framesToFlee
    val distanceClosedByEnemy = ?(Protoss.Interceptor(threat), 0.0, threat.topSpeed * framesToFlee)
    val distanceEntangled     = threat.pixelRangeAgainst(this) - threat.pixelDistanceEdge(this)
    val output                = distanceEntangled + distanceClosedByEnemy
    output
  }
  final def pixelToFireAtSimple(enemy: UnitInfo)      : Pixel = pixelToFireAt(enemy, exhaustive = false)
  final def pixelToFireAtExhaustive(enemy: UnitInfo)  : Pixel = pixelToFireAt(enemy, exhaustive = true)
  private final def pixelToFireAt(enemy: UnitInfo, exhaustive: Boolean): Pixel = {
    // Pixel selection methods:
    //
    // Hug: Use enemy pixel
    // Range: Sit at max range
    // Sight: Sit at min(range, sight range)
    // HugRange: Hug vs ranged; Range vs. melee
    // HugSight: Hug vs ranged; Sight vs. melee
    // Special: Find safe place to drop Shuttle

    // Pixel selection criteria:
    //
    // > 1 zone: Hug
    //
    // Dragoon  wide    uphill    visible   HugRange
    // Dragoon  wide    uphill    invisible Hug
    // Dragoon  wide    flat      visible   Range
    // Dragoon  wide    flat      invisible Sight
    // Dragoon  wide    downhill  visible   Range
    // Dragoon  wide    downhill  invisible Sight
    // Dragoon  narrow  uphill    visible   HugRange
    // Dragoon  narrow  uphill    invisible Hug
    // Dragoon  narrow  flat      visible   HugRange
    // Dragoon  narrow  flat      invisible HugSight
    // Dragoon  narrow  downhill  visible   HugRange
    // Dragoon  narrow  downhill  invisible HugSight
    // Reaver   wide    uphill    visible   Range
    // Reaver   wide    uphill    invisible Hug
    // Reaver   wide    flat      visible   Range
    // Reaver   wide    flat      invisible Sight
    // Reaver   wide    downhill  visible   Range
    // Reaver   wide    downhill  invisible Sight
    // Reaver   narrow  uphill    visible   Special -> Hug
    // Reaver   narrow  uphill    invisible Special -> Hug
    // Reaver   narrow  flat      visible   Special -> Hug
    // Reaver   narrow  flat      invisible Special -> Hug
    // Reaver   narrow  downhill  visible   Special -> Hug
    // Reaver   narrow  downhill  invisible Special -> Hug
    // Zealot   wide    uphill    visible   Range
    // Zealot   wide    uphill    invisible Hug
    // Zealot   wide    flat      visible   Range
    // Zealot   wide    flat      invisible Sight
    // Zealot   wide    downhill  visible   Range
    // Zealot   wide    downhill  invisible Sight
    // Zealot   narrow  uphill    visible   Range
    // Zealot   narrow  uphill    invisible Hug
    // Zealot   narrow  flat      visible   Range
    // Zealot   narrow  flat      invisible Sight
    // Zealot   narrow  downhill  visible   Range
    // Zealot   narrow  downhill  invisible Sight
    //
    // Re-sorted:

    // Zealot	  narrow	uphill  	invisible  	Hug
    // Zealot	  wide  	uphill  	invisible  	Hug
    // Zealot	  narrow	downhill	visible  	  Range
    // Zealot	  narrow	flat    	visible  	  Range
    // Zealot	  narrow	uphill  	visible  	  Range
    // Zealot	  wide  	downhill	visible  	  Range
    // Zealot	  wide  	flat    	visible  	  Range
    // Zealot	  wide  	uphill  	visible  	  Range
    // Zealot   narrow	downhill	invisible  	Sight
    // Zealot	  narrow	flat    	invisible  	Sight
    // Zealot	  wide  	downhill	invisible  	Sight
    // Zealot	  wide  	flat    	invisible  	Sight
    // Reaver	  wide  	downhill	visible  	  Range
    // Reaver	  wide  	flat    	visible  	  Range
    // Reaver	  wide  	uphill  	visible  	  Range
    // Reaver	  wide  	downhill	invisible  	Sight
    // Reaver	  wide  	flat    	invisible  	Sight
    // Reaver	  wide  	uphill  	invisible  	Special ->	Hug
    // Reaver	  narrow	downhill	invisible  	Special ->	Hug
    // Reaver	  narrow	flat    	invisible  	Special ->	Hug
    // Reaver	  narrow	uphill  	invisible  	Special ->	Hug
    // Reaver	  narrow	downhill	visible  	  Special ->	Hug
    // Reaver	  narrow	flat    	visible  	  Special ->	Hug
    // Reaver	  narrow	uphill  	visible  	  Special ->	Hug
    // Dragoon	wide    flat    	visible  	  Range
    // Dragoon	wide    downhill	visible  	  Range
    // Dragoon	wide    uphill  	visible  	  HugRange
    // Dragoon	narrow	downhill	visible  	  HugRange
    // Dragoon	narrow	flat    	visible  	  HugRange
    // Dragoon	narrow	uphill  	visible  	  HugRange
    // Dragoon	narrow  uphill  	invisible  	Hug
    // Dragoon	wide    uphill  	invisible  	Hug
    // Dragoon	wide    flat    	invisible  	Sight
    // Dragoon	wide    downhill	invisible  	Sight
    // Dragoon	narrow	downhill	invisible  	HugSight
    // Dragoon	narrow	flat    	invisible  	HugSight

    lazy val enemyZone        = enemy.zone
    lazy val enemyTile        = enemy.tile
    lazy val enemyAltitude    = enemy.altitude
    lazy val enemyVisible     = enemy.visible
    lazy val enemyDistance    = pixelDistanceEdge(enemy)
    lazy val enemyThreatens   = enemy.unitClass.canAttack(flying) || enemy.tile.enemyRangeAgainst(this) >= With.grids.enemyRangeAirGround.margin
    lazy val range            = pixelRangeAgainst(enemy)
    lazy val weOutrange       = ! enemyThreatens || enemy.pixelRangeAgainst(this) < range
    lazy val goalDistance     = Math.max(16, range + unitClass.dimensionMin + enemy.unitClass.dimensionMin - Maff.clamp(cooldownLeft * enemy.speedApproaching(pixel) * Maff.nanToOne(enemy.topSpeed / topSpeed), -64, 64))
    lazy val edge             = ?(zone == enemyZone, None, zone.edgeTo(enemy.pixel))
    lazy val zoneDistance     = if (zone == enemy.zone) 0 else if (edge.isDefined && edge.get.contains(enemyZone)) 1 else 2
    lazy val wide             = zoneDistance != 1 || edge.forall(e => e.diameterPixels > 128 || enemy.pixelDistanceCenter(e.pixelCenter) > range)
    lazy val uphill           = altitude < enemyAltitude
    lazy val downhill         = altitude > enemyAltitude
    lazy val flat             = ! uphill && ! downhill
    lazy val isReaver         = is(Protoss.Reaver)
    lazy val isMelee          = unitClass.melee
    lazy val formations       = friendly.flatMap(_.squad).map(_.formations).getOrElse(Seq.empty)
    lazy val formationPixel   = ?(friendly.exists(_.agent.shouldFight), formations.headOption, formations.lastOption).flatMap(_.apply(friendly.get))

    lazy val pixelParadrop = {
      val origin = formationPixel.getOrElse(pixel)
      Arc(enemy.pixel, origin, effectiveRangePixels, 32)
        .map(_.asPixel)
        .find(spot => {
          val spotTile  = spot.tile
          var output    = spotTile.valid
          output &&= spotTile.walkable
          output &&= (wide || spotTile.zone == enemyZone)
          output &&= spotTile.altitude >= enemyAltitude || enemy.visible
          output &&= spotTile.enemyRange < With.grids.enemyRangeAirGround.margin
          output &&= ! spotTile.zone.island || spotTile.zone == enemyZone || spotTile.zone.edges.exists(_.zones.contains(enemyZone))
          output
        })
    }

    lazy val pixelHug         = enemy.pixel.nearestTraversablePixel(this)
    lazy val pixelRange       = enemy.pixel.project(pixel, goalDistance).nearestTraversablePixel(this)
    lazy val pixelSight       = enemy.pixel.project(pixel, Math.min(goalDistance, sightPixels)).nearestTraversablePixel(this)
    lazy val pixelHugRange    = ?(weOutrange, pixelRange, pixelHug)
    lazy val pixelHugSight    = ?(weOutrange, pixelSight, pixelHug)
    lazy val pixelHugParadrop = ?(exhaustive, pixelParadrop.getOrElse(pixelRange), pixelRange)

    if (unitClass.isBuilding) {
      pixel
    } else if (zoneDistance > 1) {
      pixelHug
    } else if (flying) {
      if (enemyThreatens) {
        pixelRange
      } else {
        pixelHug
      }
    } else if (isMelee) {
      if (uphill && ! enemyVisible) {
        pixelHug
      } else if (enemyVisible) {
        pixelRange
      } else {
        pixelSight
      }
    } else if (isReaver) {
      if (wide && (enemyVisible || ! uphill)) {
        pixelRange
      } else {
        pixelHugParadrop
      }
    } else if (enemyVisible) {
      if (wide && ! uphill) {
        pixelRange
      } else {
        pixelHugRange
      }
    } else if (uphill) {
      pixelHug
    } else if (wide) {
      pixelSight
    } else {
      pixelHugSight
    }
  }
  @inline final def pixelsToSightRange(other: UnitInfo): Double = pixelDistanceEdge(other) - sightPixels
  @inline final def canStim: Boolean = unitClass.canStim && player.hasTech(Terran.Stim) && hitPoints > 10

  @inline final def moving: Boolean = velocityX != 0 || velocityY != 0
  @inline final def speed: Double = Maff.broodWarDistanceDouble(0.0, 0.0, velocityX, velocityY)
  @inline final def speedApproaching(other: UnitInfo): Double = speedApproaching(other.pixel)
  @inline final def speedApproaching(pixel: Pixel): Double = {
    val deltaXY = Force(x - pixel.x, y - pixel.y)
    val deltaV  = velocity
    val output  = - velocity.lengthFast * (deltaXY.normalize * velocity.normalize)
    output
  }
  private val _gatheringOrders = Seq(Orders.WaitForMinerals, Orders.MiningMinerals, Orders.WaitForGas, Orders.HarvestGas, Orders.MoveToMinerals, Orders.MoveToGas, Orders.ReturnGas, Orders.ReturnMinerals, Orders.ResetCollision)
  @inline final def speedApproachingEachOther(other: UnitInfo): Double = speedApproaching(other) + other.speedApproaching(this)
  @inline final def airborne  : Boolean = flying || airlifted
  @inline final def gathering : Boolean = unitClass.isWorker && _gatheringOrders.contains(order)
  @inline final def carrying  : Boolean = carryingMinerals || carryingGas
  @inline final def airlifted : Boolean = friendly.exists(_.transport.exists(_.flying))

  @inline final def presumptiveDestinationFinal : Pixel = friendly.map(_.agent.destinationFinal()).getOrElse(_presumptiveDestinationCache())
  @inline final def presumptiveDestinationNext  : Pixel = friendly.map(_.agent.destinationNext()).map(pixel.projectUpTo(_, MicroPathing.waypointDistancePixels)).getOrElse(_presumptiveStepCache())
  @inline final def presumptiveTarget     : Option[UnitInfo]  = friendly.flatMap(_.agent.toAttack).orElse(_presumptiveTargetCache())
  private val _presumptiveDestinationCache = new Cache(() =>
    ?(canMove,
      targetPixel
      .orElse(orderTargetPixel)
      .orElse(presumptiveTarget.map(pixelToFireAtSimple))
      .getOrElse(pixel),
      pixel))
  private val _presumptiveStepCache = new KeyedCache(
    () => ?(canMove,
      MicroPathing.getWaypointToPixel(this, presumptiveDestinationFinal),
      pixel),
    () => presumptiveDestinationFinal)
  private val _presumptiveTargetCache = new KeyedCache(
    () =>friendly.flatMap(_.agent.toAttack          .filter(isEnemyOf))
      .orElse(friendly.flatMap(_.agent.toAttackLast).filter(isEnemyOf))
      .orElse(friendly.flatMap(_.intent.toAttack)   .filter(isEnemyOf))
      .orElse(target                                .filter(isEnemyOf))
      .orElse(orderTarget                           .filter(isEnemyOf))
      .orElse(friendly.flatMap(_.targetsAssigned).flatMap(_.headOption))
      .orElse(matchups.targetNearest),
    () =>friendly.flatMap(_.agent.toAttack))

  @inline final def projectFrames(framesToLookAhead: Double): Pixel = pixel.projectUpTo(presumptiveDestinationNext, framesToLookAhead * topSpeed)

  def techProducing: Option[Tech]
  def upgradeProducing: Option[Upgrade]

  def painfullyIrradiated: Boolean = unitClass.canBeIrradiateBurned && irradiated

  ////////////////
  // Visibility //
  ////////////////

  def visibility: Visibility.Value
  private var _frameKnownToOpponents: Int = Forever()
  @inline final def frameKnownToOpponents: Int = _frameKnownToOpponents
  @inline final def knownToOpponents: Boolean = _frameKnownToOpponents < Forever()
  @inline final def visibleToOpponents: Boolean = isEnemy || (
    tile.visibleToEnemy
    || (unitClass.tileWidth > 1 && unitClass.tileHeight > 1 && tileArea.tiles.exists(_.visibleToEnemy))
    || With.framesSince(lastFrameTakingDamage) < Seconds(2)()
    || With.framesSince(lastFrameStartingAttack) < Seconds(2)())
  @inline final def likelyStillThere: Boolean = alive && (visible || (
    visibility == Visibility.Visible
    || visibility == Visibility.InvisibleNearby
    || visibility == Visibility.InvisibleBurrowed))
  @inline final def cloakedOrBurrowed: Boolean = cloaked || burrowed
  @inline final def effectivelyCloaked: Boolean = _effectivelyCloaked()
  private val _effectivelyCloaked = new Cache(() =>
    cloakedOrBurrowed
      && ! ensnared
      && ! plagued
      && ! ?(isOurs,
      tile.enemyDetected
        || matchups.enemies.exists(_.orderTarget.contains(this))
        || matchups.enemies.exists(_.target.contains(this))
        || With.bullets.all.exists(_.targetUnit.contains(this)),
      detected))

  @inline final def teamColor: Color =
    if      (visible)             player.colorBright
    else if (likelyStillThere)    player.colorMedium
    else if (alive)               player.colorMidnight
    else                          Colors.MidnightGray
  @inline final val unitColor: Color = Colors.hsv(hashCode % 256, 255, 128 + (hashCode / 256) % 128)

  override def apply(other: UnitInfo): Boolean = other == this
}
