package ProxyBwapi.UnitInfo

import Debugging.Visualizations.Colors
import Information.Battles.Clustering.BattleCluster
import Information.Battles.MCRS.MCRSUnit
import Information.Battles.Prediction.Simulation.ReportCard
import Information.Battles.Types.{BattleLocal, Team}
import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points._
import Mathematics.PurpleMath
import Mathematics.Shapes.Ring
import Micro.Actions.Combat.Targeting.Target
import Micro.Coordination.Pathing.MicroPathing
import Micro.Matchups.MatchupAnalysis
import Micro.Squads.Squad
import Performance.{Cache, KeyedCache}
import Planning.Prioritized
import Planning.UnitMatchers.UnitMatcher
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade
import Utilities._
import bwapi._

abstract class UnitInfo(val bwapiUnit: bwapi.Unit, val id: Int) extends UnitProxy {

  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None

  @inline final override val hashCode: Int = id + With.frame * 10000
  @inline final override def toString: String = f"${if (isFriendly) "Our" else if (isEnemy) "Foe" else "Neutral"} $unitClass ${if (selected) "*" else ""} #$id $hitPoints/${unitClass.maxHitPoints} ${if (shieldPoints > 0) f"($shieldPoints/${unitClass.maxShields})" else ""} $tile $pixel"

  @inline final def is(unitMatcher: UnitMatcher): Boolean = unitMatcher.apply(this)
  @inline final def isPrerequisite(unitMatcher: UnitMatcher): Boolean = (
    unitMatcher(this)
      || unitMatcher == Zerg.Hatchery && isAny(Zerg.Lair, Zerg.Hive)
      || unitMatcher == Zerg.Lair && is(Zerg.Hatchery)
      || unitMatcher == Zerg.Spire && is(Zerg.GreaterSpire))
  @inline final def isNone(unitMatchers: UnitMatcher*): Boolean = ! unitMatchers.exists(_(this))
  @inline final def isAny(unitMatchers: UnitMatcher*): Boolean = unitMatchers.exists(_(this))
  @inline final def isAll(unitMatchers: UnitMatcher*): Boolean = unitMatchers.forall(_(this))

  val frameDiscovered             : Int = With.frame
  val initialHitPoints            : Int = bwapiUnit.getHitPoints
  val initialShields              : Int = bwapiUnit.getShields
  var completionFrame             : Int = Forever() // Can't use unitClass during construction
  var lastHitPoints               : Int = _
  var lastShieldPoints            : Int = _
  var lastMatrixPoints            : Int = _
  var lastCooldown                : Int = _
  var lastFrameTakingDamage       : Int = - Forever()
  var lastFrameTryingToAttack     : Int = - Forever()
  var lastFrameStartingAttack     : Int = - Forever()
  var hasEverBeenCompleteHatch    : Boolean = false // Stupid AIST hack fix for detecting whether a base is mineable
  private var lastUnitClass       : UnitClass = _
  private val previousPixels      : Array[Pixel] = Array.fill(24)(new Pixel(bwapiUnit.getPosition))
  private var previousPixelIndex  : Int = 0
  @inline final def previousPixel(framesAgo: Int): Pixel = previousPixels((previousPixels.length + previousPixelIndex - Math.min(previousPixels.length, framesAgo)) % previousPixels.length)
  def update() {
    if (cooldownLeft > lastCooldown) lastFrameStartingAttack = With.frame
    if (totalHealth < lastHitPoints + lastShieldPoints + lastMatrixPoints) lastFrameTakingDamage = With.frame
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
    hasEverBeenCompleteHatch ||= complete && is(Zerg.Hatchery)
    previousPixelIndex = (previousPixelIndex + 1) % previousPixels.length
    previousPixels(previousPixelIndex) = pixel
  }

  @inline final def aliveAndComplete: Boolean = alive && complete
  @inline final def energyMax     : Int = unitClass.maxEnergy // TODO: Count effects of upgrades
  @inline final def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  @inline final def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0

  // When a Larva is about to morph, but hasn't turned into an egg, remainingCompletionFrames is ZERO
  @inline final def completeOrNearlyComplete: Boolean = complete || (remainingCompletionFrames < With.latency.framesRemaining && ( ! isAny(Zerg.Larva, Zerg.Hydralisk, Zerg.Mutalisk)))

  lazy val isBlocker: Boolean = (unitClass.isMinerals || unitClass.isGas) && gasLeft + mineralsLeft < With.configuration.blockerMineralThreshold

  @inline final def subjectiveValue: Double = subjectiveValueCache()
  private val subjectiveValueCache = new Cache(() =>
    unitClass.subjectiveValue
      + scarabs * Protoss.Scarab.subjectiveValue
      + (if (is(Protoss.Carrier)) friendly.map(_.interceptorCount).getOrElse(8) * Protoss.Interceptor.subjectiveValue else 0)
      + (if (unitClass.isTransport) friendly.map(_.loadedUnits.map(_.subjectiveValue).sum).sum else 0))

  @inline final def remainingOccupationFrames: Int = Math.max(
    remainingCompletionFrames,  Math.max(
    remainingTechFrames,        Math.max(
    remainingUpgradeFrames,     Math.max(
    remainingTrainFrames,
    addon.map(_.remainingCompletionFrames).getOrElse(0)
  ))))

  private var producer: Option[Prioritized] = None
  @inline final def setProducer(plan: Prioritized) {
    producer = Some(plan)
  }
  @inline final def getProducer: Option[Prioritized] = {
    producer.filter(_.isPrioritized)
  }

  @inline final def x           : Int   = pixel.x
  @inline final def y           : Int   = pixel.y
  @inline final def left        : Int   = x - unitClass.dimensionLeft
  @inline final def right       : Int   = x + unitClass.dimensionRight
  @inline final def top         : Int   = y - unitClass.dimensionUp
  @inline final def bottom      : Int   = y + unitClass.dimensionDown
  @inline final def topLeft     : Pixel = Pixel(left, top)
  @inline final def topRight    : Pixel = Pixel(right, top)
  @inline final def bottomLeft  : Pixel = Pixel(left, bottom)
  @inline final def bottomRight : Pixel = Pixel(right, bottom)
  @inline final def corners   : Vector[Pixel] = Vector(topLeft, topRight, bottomLeft, bottomRight)
  @inline final def tiles     : Seq[Tile]     = cacheTiles() // TODO: Set this on type/pixel change
  @inline final def tileArea  : TileRectangle = cacheTileArea() // TODO: Set this on type/pixel change
  @inline final def addonArea : TileRectangle = TileRectangle(Tile(0, 0), Tile(2, 2)).add(tileTopLeft).add(4,1)

  private def tileCacheDuration: Int = { if (unitClass.isBuilding) (if (unitClass.canFly) 24 * 5 else 24 * 60) else 1 }
  private lazy val cacheTileArea = new Cache(() => unitClass.tileArea.add(tileTopLeft), refreshPeriod = tileCacheDuration)
  private lazy val cacheTiles = new Cache(() => cacheTileArea().tiles.toVector, refreshPeriod = tileCacheDuration)

  @inline final def zone: Zone = if (unitClass.isBuilding) tileTopLeft.zone else tile.zone // Hack to get buildings categorized in zone they were intended to be constructed in
  @inline final def base: Option[Base] = tile.base

  @inline final def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  @inline final def pixelRangeAir: Double = pixelRangeAirCache()
  private val pixelRangeAirCache = new Cache(() =>
    unitClass.pixelRangeAir +
      (if (is(Terran.Bunker))                                                 32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Goliath)   && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  @inline final def pixelRangeGround: Double = pixelRangeGroundCache()
  private val pixelRangeGroundCache = new Cache(() =>
    unitClass.pixelRangeGround +
      (if (is(Terran.Bunker))                                               32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  @inline final def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)

  @inline final def canTraverse(pixel: Pixel): Boolean = pixel.traversableBy(this)
  @inline final def canTraverse(tile: Tile): Boolean = tile.traversableBy(this)

  @inline final def pixelStart                                                : Pixel   = Pixel(left, top)
  @inline final def pixelEnd                                                  : Pixel   = Pixel(right, bottom)
  @inline final def pixelStartAt            (at: Pixel)                       : Pixel   = at.subtract(pixel).add(left, top)
  @inline final def pixelEndAt              (at: Pixel)                       : Pixel   = at.subtract(pixel).add(right, bottom)
  @inline final def pixelDistanceCenter     (otherPixel:  Pixel)              : Double  = pixel.pixelDistance(otherPixel)
  @inline final def pixelDistanceCenter     (otherUnit:   UnitInfo)           : Double  = pixelDistanceCenter(otherUnit.pixel)
  @inline final def pixelDistanceShooting   (other:       UnitInfo)           : Double  = if (is(Protoss.Reaver) && zone != other.zone) pixelDistanceTravelling(other.pixel) else pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  @inline final def pixelDistanceEdge       (other:       UnitInfo)           : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  @inline final def pixelDistanceEdge       (other: UnitInfo, otherAt: Pixel) : Double  = pixelDistanceEdge(otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRight, other.unitClass.dimensionDown))
  @inline final def pixelDistanceEdge       (oStart: Pixel, oEnd: Pixel)      : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, oStart, oEnd)
  @inline final def pixelDistanceEdge       (destination: Pixel)              : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, destination, destination)
  @inline final def pixelDistanceEdge       (other: UnitInfo, usAt: Pixel, otherAt: Pixel): Double  = PurpleMath.broodWarDistanceBox(usAt.subtract(unitClass.dimensionLeft, unitClass.dimensionUp), usAt.add(unitClass.dimensionRight, unitClass.dimensionDown), otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRight, other.unitClass.dimensionDown))
  @inline final def pixelDistanceEdgeFrom   (other: UnitInfo, usAt: Pixel)    : Double  = other.pixelDistanceEdge(this, usAt)
  @inline final def pixelDistanceSquared    (otherUnit:   UnitInfo)           : Double  = pixelDistanceSquared(otherUnit.pixel)
  @inline final def pixelDistanceSquared    (otherPixel:  Pixel)              : Double  = pixel.pixelDistanceSquared(otherPixel)
  @inline final def pixelDistanceTravelling (destination: Pixel)              : Double  = pixelDistanceTravelling(pixel, destination)
  @inline final def pixelDistanceTravelling (destination: Tile)               : Double  = pixelDistanceTravelling(pixel, destination.pixelCenter)
  @inline final def pixelDistanceTravelling (from: Pixel, to: Pixel)          : Double  = if (flying) from.pixelDistance(to) else from.nearestWalkableTile.groundPixels(to.nearestWalkableTile)
  @inline final def pixelDistanceTravelling (from: Tile,  to: Tile)           : Double  = if (flying) from.pixelCenter.pixelDistance(to.pixelCenter) else from.nearestWalkableTile.groundPixels(to.nearestWalkableTile)

  @inline final def canMove: Boolean = canMoveCache()
  private val canMoveCache = new Cache(() =>
    (unitClass.canMove || (unitClass.isBuilding && flying))
    && unitClass.topSpeed > 0
    && canDoAnything
    && ! burrowed
    && ! is(Terran.SiegeTankSieged))
  @inline final def velocity: Force = Force(velocityX, velocityY)
  @inline final def topSpeed: Double = if (canMove) topSpeedPossibleCache() else 0
  @inline final def topSpeedPossible: Double = topSpeedPossibleCache()
  private val topSpeedPossibleCache = new Cache(() =>
    (if (ensnared) 0.5 else 1.0) * // TODO: Is this the multiplier?
    (if (stimmed) 1.5 else 1.0) * (
    (if (is(Terran.SiegeTankSieged)) Terran.SiegeTankUnsieged.topSpeed else unitClass.topSpeed)
    * (if (
      (is(Terran.Vulture)   && player.hasUpgrade(Terran.VultureSpeed))    ||
      (is(Protoss.Observer) && player.hasUpgrade(Protoss.ObserverSpeed))  ||
      (is(Protoss.Scout)    && player.hasUpgrade(Protoss.ScoutSpeed))     ||
      (is(Protoss.Shuttle)  && player.hasUpgrade(Protoss.ShuttleSpeed))   ||
      (is(Protoss.Zealot)   && player.hasUpgrade(Protoss.ZealotSpeed))    ||
      (is(Zerg.Overlord)    && player.hasUpgrade(Zerg.ZerglingSpeed))     ||
      (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskSpeed))    ||
      (is(Zerg.Ultralisk)   && player.hasUpgrade(Zerg.UltraliskSpeed)))
      1.5 else 1.0)))

  @inline final def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tile, tiles)
  @inline final def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixel, pixels)

  @inline final def isTransport: Boolean = if (is(Zerg.Overlord)) player.hasUpgrade(Zerg.OverlordDrops) else unitClass.isTransport

  @inline final def detectionRangePixels: Int = if (unitClass.isDetector) (if (unitClass.isBuilding) 32 * 7 else sightPixels) else 0
  @inline final def sightPixels: Int = sightRangePixelsCache()
  private val sightRangePixelsCache = new Cache(() =>
    if (blind) 32 else
    unitClass.sightRangePixels +
      (if (
        (is(Terran.Ghost)     && player.hasUpgrade(Terran.GhostVisionRange))      ||
        (is(Protoss.Observer) && player.hasUpgrade(Protoss.ObserverVisionRange))  ||
        (is(Protoss.Scout)    && player.hasUpgrade(Protoss.ScoutVisionRange))     ||
        (is(Zerg.Overlord)    && player.hasUpgrade(Zerg.OverlordVisionRange)))
      64 else 0))

  @inline final def altitude: Double = tile.altitude

  val arrivalFrame = new Cache(() => {
    val home        = With.geography.home.pixelCenter
    val classSpeed  = unitClass.topSpeed
    val travelTime  = Math.min(24 * 60 * 60,
      if (canMove)
        framesToTravelTo(home)
      else if (classSpeed > 0)
        (pixelDistanceTravelling(home) / classSpeed).toInt
      else
        Int.MaxValue)
    val completionTime  = PurpleMath.clamp(completionFrame, With.frame, With.frame + unitClass.buildFrames)
    val arrivalTime     = completionTime + travelTime
    arrivalTime
  })

  ////////////
  // Combat //
  ////////////

  @inline final def squads: Seq[Squad] = foreign.map(_.enemyOfSquads).orElse(friendly.map(_.squad.toSeq)).getOrElse(Seq.empty)
  @inline final def battle: Option[BattleLocal] = With.battles.byUnit.get(this).orElse(With.matchups.entrants.find(_._2.contains(this)).map(_._1))
  @inline final def team: Option[Team] = battle.map(_.teamOf(this))
  @inline final def report: Option[ReportCard] = battle.flatMap(_.predictionAttack.debugReport.get(this))
  var matchups: MatchupAnalysis = MatchupAnalysis(this)
  val mcrs: MCRSUnit = new MCRSUnit(this)
  var clusteringEnabled: Boolean = _
  var clusteringRadiusSquared: Double = _
  var cluster: Option[BattleCluster] = _

  val targetBaseValue = new Cache(() => Target.getTargetBaseValue(this), 24)

  @inline final def totalHealth: Int = hitPoints + shieldPoints + matrixPoints
  @inline final def armorHealth: Int = armorHealthCache()
  @inline final def armorShield: Int = armorShieldsCache()
  private lazy val armorHealthCache   = new Cache(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  private lazy val armorShieldsCache  = new Cache(() => player.getUpgradeLevel(Protoss.Shields))

  @inline final def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  @inline final def dpfAir    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir.toDouble
  @inline final def dpfGround : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround.toDouble

  @inline final def attacksAgainstAir: Int = attacksAgainstAirCache()
  private val attacksAgainstAirCache = new Cache(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = friendly.map(_.interceptorCount).getOrElse(8)
    output
  })

  @inline final def attacksAgainstGround: Int = attacksAgainstGroundCache()
  private val attacksAgainstGroundCache = new Cache(() => {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    if (output == 0  && is(Terran.Bunker))   output = friendly.map(_.loadedUnits.size).getOrElse(4)
    if (output == 0  && is(Protoss.Carrier))  output = friendly.map(_.interceptorCount).getOrElse(8)
    if (output == 0  && is(Protoss.Reaver))   output = 1
    output
  })

  @inline final def cooldownLeft : Int = (
    //TODO: Ensnare
    (if (complete)
      Math.max(
        friendly.filter(_.transport.exists(_.flying)).map(unused => cooldownMaxAirGround / 2).getOrElse(0),
        Math.max(
          cooldownAir,
          cooldownGround))
    else remainingCompletionFrames)
    + friendly
      .filter(u => u.is(Protoss.Reaver) && u.scarabs == 0)
      .map(_.trainee.map(_.remainingCompletionFrames).getOrElse(Protoss.Scarab.buildFrames))
      .getOrElse(0))

  @inline final def cooldownMaxAir    : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxGround : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxAirGround: Int = Math.max(
    if (unitClass.attacksAir)     cooldownMaxAir    else 0,
    if (unitClass.attacksGround)  cooldownMaxGround else 0)
  @inline final def cooldownMaxAgainst(enemy: UnitInfo): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround
  @inline final def pixelRangeAgainst(enemy: UnitInfo): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  @inline final def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)

  @inline final def hitChanceAgainst(enemy : UnitInfo, from: Option[Pixel] = None, to: Option[Pixel] = None): Double = if (guaranteedToHit(enemy, from, to)) 1.0 else 0.47
  @inline final def guaranteedToHit(enemy : UnitInfo, from: Option[Pixel] = None, to: Option[Pixel] = None): Boolean = {
    val tileFrom  = from.getOrElse(pixel)       .tile
    val tileTo    =   to.getOrElse(enemy.pixel) .tile
    flying || enemy.flying || unitClass.unaffectedByDarkSwarm || tileFrom.altitude >= tileTo.altitude
  }

  @inline final def damageTypeAgainst (enemy: UnitInfo)  : Damage.Type  = if (enemy.flying) unitClass.airDamageType    else unitClass.groundDamageType
  @inline final def attacksAgainst    (enemy: UnitInfo)  : Int          = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround

  @inline final def damageScaleAgainstHitPoints(enemy: UnitInfo): Double = {
    if (dpfAir    <= 0 && enemy.flying)   return 0.0
    if (dpfGround <= 0&& ! enemy.flying)  return 0.0
    Damage.scaleBySize(damageTypeAgainst(enemy), enemy.unitClass.size)
  }

  @inline final def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  @inline final def damageOnHitGround   : Int = damageOnHitGroundCache()
  @inline final def damageOnHitAir      : Int = damageOnHitAirCache()
  private val damageOnHitGroundCache  = new Cache(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new Cache(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)

  @inline final def damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  @inline final def damageOnNextHitAgainst(
    enemy   : UnitInfo,
    shields : Option[Int] = None,
    from    : Option[Pixel] = None,
    to      : Option[Pixel] = None)
      : Int = {
    val enemyShieldPoints       = shields.getOrElse(enemy.shieldPoints)
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShieldPoints + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealthScale     = damageScaleAgainstHitPoints(enemy)
    val damageToHealth          = Math.max(0.0, (damageAssignedToHealth - enemy.armorHealth * hits) * damageToHealthScale)
    val damageDealtTotal        = damageToHealth + damageToShields
    val hitChance               = hitChanceAgainst(enemy, from, to)
    val output                  = (hitChance * Math.max(1.0, damageDealtTotal)).toInt
    output
  }

  @inline final def dpfOnNextHitAgainst(enemy: UnitInfo): Double =
    if (unitClass.suicides) damageOnNextHitAgainst(enemy) else {
      val cooldownVs = cooldownMaxAgainst(enemy)
      if (cooldownVs == 0) 0.0 else damageOnNextHitAgainst(enemy).toDouble / cooldownVs
    }

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

  @inline final def canAttack(enemy: UnitInfo): Boolean = (
    canAttack
    && enemy.canBeAttacked
    && (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
    && ! enemy.effectivelyCloaked
    && (enemy.unitClass.triggersSpiderMines || ! is(Terran.SpiderMine))
    && (unitClass.unaffectedByDarkSwarm || ! enemy.underDarkSwarm))

  @inline final def canAttackAir: Boolean = canAttack && attacksAgainstAir > 0
  @inline final def canAttackGround: Boolean = canAttack && attacksAgainstGround > 0
  @inline final def canBurrow: Boolean = canDoAnything && (is(Zerg.Lurker) || (player.hasTech(Zerg.Burrow) && isAny(Zerg.Drone, Zerg.Zergling, Zerg.Hydralisk, Zerg.Defiler)))

  val widthSlotProjected  = new Cache(() => team.map(team => if (flying) team.centroidAir() else PurpleMath.projectedPointOnLine(pixel, team.centroidGround(), team.lineWidth())).getOrElse(pixel))
  val widthSlotIdeal      = new Cache(() => team.map(team => if (flying) team.centroidAir() else team.widthOrder().zipWithIndex.find(_._1 == this).map(p => team.centroidGround().project(team.lineWidth(), team.widthIdeal() * (team.widthOrder().size / 2 - p._2) / team.widthOrder().size)).getOrElse(widthSlotProjected())).getOrElse(pixel))
  val widthContribution   = new Cache(() => team.map(_.centroidOf(this).pixelDistance(widthSlotProjected()) * 2))
  val depthMeasuredFrom   = new Cache(() => presumptiveTarget.map(_.pixel).orElse(team.map(_.opponent.vanguard())))
  val depthCurrent        = new Cache(() => depthMeasuredFrom().map(pixelDistanceCenter(_) - effectiveRangePixels))
  val confidence          = new Cache(() => if (matchups.threats.isEmpty) 1.0 else battle.flatMap(_.judgement.map(_.confidence)).getOrElse(1.0))

  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  @inline final def framesToBeReadyForAttackOrder: Int = cooldownLeft - With.latency.framesRemaining - With.reaction.agencyMin
  @inline final def readyForAttackOrder: Boolean = canAttack && framesToBeReadyForAttackOrder <= 0

  @inline final def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  @inline final def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  @inline final def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  @inline final def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  @inline final def pixelReachAgainst (framesAhead: Int, enemy: UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  @inline final def inRangeToAttack(enemy: UnitInfo)                    : Boolean = pixelDistanceEdge(enemy)          <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy)          > pixelRangeMin)
  @inline final def inRangeToAttack(enemy: UnitInfo, enemyAt: Pixel)    : Boolean = pixelDistanceEdge(enemy, enemyAt) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy, enemyAt) > pixelRangeMin)
  @inline final def inRangeToAttack(enemy: UnitInfo, framesAhead: Int)  : Boolean = inRangeToAttack(enemy, enemy.projectFrames(framesAhead))
  @inline final def inRangeToAttack(enemy: UnitInfo, usAt: Pixel, to: Pixel): Boolean = { val d = pixelDistanceEdge(enemy, usAt, to); d <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || d > pixelRangeMin) }
  @inline final def inRangeToAttackFrom(enemy: UnitInfo, usAt: Pixel)   : Boolean = pixelDistanceEdgeFrom(enemy, usAt) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdgeFrom(enemy, usAt) > pixelRangeMin)
  @inline final def pixelsToGetInRange(enemy: UnitInfo)                 : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy) - pixelRangeAgainst(enemy)) else LightYear()
  @inline final def pixelsToGetInRange(enemy: UnitInfo, enemyAt: Pixel) : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy, enemyAt) - pixelRangeAgainst(enemy)) else LightYear()
  @inline final def framesToTravelTo(destination: Pixel)  : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  @inline final def framesToTravelPixels(pixels: Double)  : Int = (if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels / topSpeedPossible).toInt) else Forever()) + (if (burrowed || is(Terran.SiegeTankSieged)) 24 else 0)
  @inline final def framesToTurnTo    (radiansTo: Double)   : Double = unitClass.framesToTurn(PurpleMath.normalizeAroundZero(PurpleMath.radiansTo(angleRadians, radiansTo)))
  @inline final def framesToTurnTo    (pixelTo: Pixel)      : Double = framesToTurnTo(pixel.radiansTo(pixelTo))
  @inline final def framesToTurnFrom  (pixelTo: Pixel)      : Double = framesToTurnTo(pixelTo.radiansTo(pixel))
  @inline final def framesToTurnTo    (enemyFrom: UnitInfo) : Double = framesToTurnTo(enemyFrom.pixel.radiansTo(pixel))
  @inline final def framesToTurnFrom  (enemyFrom: UnitInfo) : Double = framesToTurnTo(pixel.radiansTo(enemyFrom.pixel))
  @inline final def framesToStopRightNow: Double = if (unitClass.isFlyer || unitClass.floats) PurpleMath.clamp(PurpleMath.nanToZero(framesToAccelerate * speed / topSpeed), 0.0, framesToAccelerate) else 0.0
  @inline final def framesToAccelerate: Double = PurpleMath.clamp(PurpleMath.nanToZero((topSpeed - speed) / unitClass.accelerationFrames), 0, unitClass.accelerationFrames)
  @inline final def framesToGetInRange(enemy: UnitInfo)                 : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy)) else Forever()
  @inline final def framesToGetInRange(enemy: UnitInfo, enemyAt: Pixel) : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy, enemyAt)) else Forever()
  @inline final def framesBeforeAttacking(enemy: UnitInfo)              : Int = framesBeforeAttacking(enemy, enemy.pixel)
  @inline final def framesBeforeAttacking(enemy: UnitInfo, at: Pixel)   : Int = if (canAttack(enemy))  Math.max(cooldownLeft, framesToGetInRange(enemy)) else Forever()
  @inline final def pixelsOfEntanglement(threat: UnitInfo): Double = {
    val speedTowardsThreat    = speedApproaching(threat)
    val framesToStopMe        = if(speedTowardsThreat <= 0) 0.0 else framesToStopRightNow
    val framesToFlee          = framesToStopMe + unitClass.framesToTurn180 + With.latency.framesRemaining + With.reaction.agencyAverage
    val distanceClosedByMe    = speedTowardsThreat * framesToFlee
    val distanceClosedByEnemy = if (threat.is(Protoss.Interceptor)) 0.0 else (threat.topSpeed * framesToFlee)
    val distanceEntangled     = threat.pixelRangeAgainst(this) - threat.pixelDistanceEdge(this)
    val output                = distanceEntangled + distanceClosedByEnemy
    output
  }
  @inline final def pixelToFireAt(enemy: UnitInfo): Pixel = pixelToFireAt(enemy, exhaustive = false)
  @inline final def pixelToFireAt(enemy: UnitInfo, exhaustive: Boolean): Pixel = {
    if (unitClass.melee) return enemy.pixel
    if (With.reaction.sluggishness > 1 || ! canMove) {
      return if (inRangeToAttack(enemy)) pixel else enemy.pixel.project(pixel, pixelRangeAgainst(enemy))
    }
    val range = pixelRangeAgainst(enemy)
    val distance = pixelDistanceEdge(enemy)
    val distanceSquared = pixelDistanceSquared(enemy)
    val enemyAltitude = enemy.altitude
    val veryFarSquared = With.mapPixelPerimeter * With.mapPixelPerimeter
    val altitudeMatters = ! flying && ! enemy.flying && ! unitClass.melee
    def badAltitudePenalty(pixel: Pixel): Int = if (altitudeMatters && pixel.altitude < enemyAltitude) veryFarSquared  else 0
    def goodAltitudeBonus(pixel: Pixel): Double = if ( ! altitudeMatters || pixel.altitude > enemyAltitude) 1 else 0
    if (enemy.visible || (flying && sightPixels >= range)) {
      if (range >= distance && ( ! exhaustive || ! altitudeMatters)) return pixel
      // First, check if the simplest possible spot is acceptable
      val pixelClose  = enemy.pixel.project(pixel, range)
      val usToEnemyGapFacingRight    =    enemy.x - enemy.unitClass.dimensionLeft   - x - unitClass.dimensionRight
      val usToEnemyGapFacingLeft     = - (enemy.x - enemy.unitClass.dimensionRight  - x - unitClass.dimensionLeft)
      val usToEnemyGapFacingDown     =    enemy.y - enemy.unitClass.dimensionUp     - y - unitClass.dimensionDown
      val usToEnemyGapFacingUp       = - (enemy.y - enemy.unitClass.dimensionDown   - y - unitClass.dimensionUp)
      val pixelFar = pixelClose
        .add(
          // DOUBLE CHECK CLAMPS
          PurpleMath.clamp(usToEnemyGapFacingLeft, 0, enemy.unitClass.dimensionRight + unitClass.dimensionLeft)  - PurpleMath.clamp(usToEnemyGapFacingRight, 0, enemy.unitClass.dimensionLeft + unitClass.dimensionRight),
          PurpleMath.clamp(usToEnemyGapFacingUp,   0, enemy.unitClass.dimensionDown  + unitClass.dimensionUp)    - PurpleMath.clamp(usToEnemyGapFacingDown, 0, enemy.unitClass.dimensionUp + unitClass.dimensionDown))
      if ( ! exhaustive || With.reaction.sluggishness > 0) {
        return pixelFar.nearestTraversableBy(this)
      }
      else if (badAltitudePenalty(pixelFar) == 0 && goodAltitudeBonus(pixelFar) > 0) {
        return pixelFar
      }
      // Search for the ideal firing position
      val offset = pixel.offsetFromTileCenter
      val ringPixels =
        Ring
          .points(range.toInt / 32)
          .map(enemy.tile.add(_).pixelCenter.add(offset))
          .filter(p => canTraverse(p) && pixelDistanceSquared(p) < distanceSquared)
      val ringSpot = ByOption.minBy(ringPixels)(p => pixelDistanceSquared(p) * (2 - goodAltitudeBonus(p)) - badAltitudePenalty(p))
      val output = ringSpot.getOrElse(pixelFar.nearestTraversableBy(this))
      return output
    }
    // If the enemy isn't visible (likely uphill) we not only need to get in physical range, but altitude-adjusted sight range as well
    val sightPixel = enemy.pixel.projectUpTo(pixel, Math.min(sightPixels, range))
    PixelRay(sightPixel, enemy.pixel)
      .find(t => t.traversableBy(this) && ( ! altitudeMatters || t.altitude >= enemyAltitude))
      .map(_.pixelCenter)
      .getOrElse(enemy.pixel.nearestTraversableBy(this))
  }
  @inline final def canSee(other: UnitInfo): Boolean = (
    (sightPixels >= pixelDistanceEdge(other) && (flying|| altitude >= other.altitude))
    || (tile.tileDistanceSquared(other.tile) < 4 && tile.adjacent9.contains(other.tile)))

  @inline final def canStim: Boolean = unitClass.canStim && player.hasTech(Terran.Stim) && hitPoints > 10

  @inline final def moving: Boolean = velocityX != 0 || velocityY != 0
  @inline final def speed: Double = velocity.lengthFast
  @inline final def speedApproaching(other: UnitInfo): Double = speedApproaching(other.pixel)
  @inline final def speedApproaching(pixel: Pixel): Double = {
    val deltaXY = Force(x - pixel.x, y - pixel.y)
    val deltaV  = velocity
    val output  = - velocity.lengthFast * (deltaXY.normalize * velocity.normalize)
    output
  }
  @inline final def speedApproachingEachOther(other: UnitInfo): Double = speedApproaching(other) + other.speedApproaching(this)
  @inline final def airborne: Boolean = flying || friendly.exists(_.transport.exists(_.flying))
  @inline final def gathering: Boolean = unitClass.isWorker && orderTarget.exists(_.unitClass.isResource)
  @inline final def gatheringMinerals: Boolean = unitClass.isWorker && orderTarget.exists(_.unitClass.isMinerals)
  @inline final def gatheringGas: Boolean = unitClass.isWorker && orderTarget.exists(_.unitClass.isGas) && ! constructing
  @inline final def carrying: Boolean = carryingMinerals || carryingGas

  @inline final def presumptiveDestination: Pixel = if (isOurs) calculatePresumptiveDestination else presumptiveDestinationCached()
  @inline final def presumptiveStep: Pixel = if (isOurs) MicroPathing.getWaypointToPixel(this, presumptiveDestination) else presumptiveStepCached()
  @inline final def presumptiveTarget: Option[UnitInfo] = if (isOurs) calculatePresumptiveTarget else presumptiveTargetCached()
  @inline final def projectFrames(framesToLookAhead: Double): Pixel = pixel.projectUpTo(presumptiveStep, framesToLookAhead * topSpeed)
  val presumptiveDestinationCached = new KeyedCache(() => calculatePresumptiveDestination, () => friendly.map(_.agent.destination))
  val presumptiveStepCached = new KeyedCache(() => MicroPathing.getWaypointToPixel(this, presumptiveDestinationCached()), () => presumptiveDestinationCached(), 240)
  val presumptiveTargetCached = new KeyedCache(() => calculatePresumptiveTarget, () => friendly.map(_.agent.toAttack))
  private def calculatePresumptiveDestination: Pixel =
    if (canMove)
      friendly.flatMap(_.agent.toTravel)
        .orElse(targetPixel)
        .orElse(orderTargetPixel)
        .orElse(presumptiveTarget.map(pixelToFireAt))
        .getOrElse(pixel)
    else pixel
  private def calculatePresumptiveTarget: Option[UnitInfo] =
    friendly.flatMap(_.agent.toAttack)
      .orElse(target)
      .orElse(orderTarget)
      .orElse(ByOption.minBy(matchups.targets)(_.pixelDistanceEdge(targetPixel.orElse(orderTargetPixel).getOrElse(pixel))))

  @inline final def isBeingViolent: Boolean = {
    unitClass.isStaticDefense ||
    cooldownLeft > 0          ||
    orderTarget.exists(isEnemyOf)
  }

  @inline final def isBeingViolentTo(victim: UnitInfo): Boolean = {
    isBeingViolent &&
    isEnemyOf(victim) &&
    canAttack(victim) &&
    target.forall(_ == victim) &&
    framesToGetInRange(victim) < 48
  }

  def techProducing: Option[Tech]
  def upgradeProducing: Option[Upgrade]

  ////////////////
  // Visibility //
  ////////////////

  def visibility: Visibility.Value
  @inline final def visibleToOpponents: Boolean = if (isEnemy) true else (
    tile.visibleToEnemy
    || With.framesSince(lastFrameTakingDamage) < Seconds(2)()
    || With.framesSince(lastFrameStartingAttack) < Seconds(2)())
  @inline final def likelyStillThere: Boolean = alive && visible || (
    visibility == Visibility.Visible
    || visibility == Visibility.InvisibleBurrowed
    || visibility == Visibility.InvisibleNearby)
  @inline final def cloakedOrBurrowed: Boolean = cloaked || burrowed
  @inline final def effectivelyCloaked: Boolean = (
    cloakedOrBurrowed
    && ! ensnared
    && ! plagued
    && (if (isFriendly) ! tile.enemyDetected else ! detected))
  
  @inline final def isOurs     : Boolean = player.isUs
  @inline final def isNeutral  : Boolean = player.isNeutral
  @inline final def isFriendly : Boolean = player.isAlly || isOurs
  @inline final def isEnemy    : Boolean = player.isEnemy
  @inline final def isEnemyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
  @inline final def isAllyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isFriendly) || (isEnemy && otherUnit.isEnemy)
  
  @inline final def teamColor: Color =
    if      (visible)             player.colorBright
    else if (likelyStillThere)    player.colorMedium
    else if (alive)               player.colorMidnight
    else                          Colors.MidnightGray
  @inline final val unitColor: Color = Colors.hsv(hashCode % 256, 255, 128 + (hashCode / 256) % 128)
}
