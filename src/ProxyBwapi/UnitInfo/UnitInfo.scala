package ProxyBwapi.UnitInfo

import Debugging.Visualizations.Colors
import Information.Battles.Types.BattleLocal
import Information.Geography.Types.{Base, Zone}
import Information.Grids.AbstractGrid
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Information.Kill
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.PurpleMath
import Micro.Matchups.MatchupAnalysis
import Performance.Cache
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import bwapi._

import scala.collection.mutable

abstract class UnitInfo(baseUnit: bwapi.Unit, id: Int) extends UnitProxy(baseUnit, id) {
  
  //////////////
  // Identity //
  //////////////
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString: String = {
    unitClass.toString + " #" +
    id + " " +
    hitPoints + "/" + unitClass.maxHitPoints + " " +
    (if (shieldPoints > 0) "(" + shieldPoints + "/" + unitClass.maxShields + ") " else "") +
    tileIncludingCenter.toString + " " + pixelCenter.toString
  }
  
  def is(unitMatcher: UnitMatcher): Boolean = unitMatcher.accept(this)
  def isNone(unitMatchers: UnitMatcher*): Boolean = ! unitMatchers.exists(_.accept(this))
  def isAny(unitMatchers: UnitMatcher*): Boolean = unitMatchers.exists(_.accept(this))
  def isAll(unitMatchers: UnitMatcher*): Boolean = unitMatchers.forall(_.accept(this))
  
  
  //////////////////
  // Statefulness //
  //////////////////
  
  val frameDiscovered           : Int = With.frame
  var completionFrame           : Int = Int.MaxValue // Can't use unitClass during construction
  var lastHitPoints             : Int = _
  var lastShieldPoints          : Int = _
  var lastDefensiveMatrixPoints : Int = _
  var lastCooldown              : Int = _
  var lastFrameTakingDamage     : Int = _
  var lastFrameTryingToMove     : Int = _
  var lastFrameTryingToAttack   : Int = _
  var lastFrameStartingAttack   : Int = _
  var framesFailingToMove       : Int = 0
  var framesFailingToAttack     : Int = 0
  var lastAttacker              : Option[UnitInfo] = None
  def lastTotalHealthPoints: Int = lastHitPoints + lastShieldPoints + lastDefensiveMatrixPoints
  
  def creditKill(kill: Kill) { kills += kill }
  val kills: mutable.ArrayBuffer[Kill] = new mutable.ArrayBuffer[Kill]

  def updateCommon() {
    val thisFrame = With.frame
    if (totalHealth < lastTotalHealthPoints) {
      lastFrameTakingDamage = thisFrame
    }
    if (cooldownLeft > lastCooldown) {
      lastFrameStartingAttack = thisFrame
    }
    val moving = velocityX != 0 || velocityY != 0
    lazy val couldMove          = unitClass.canMove
    lazy val tryingToMove       = friendly.flatMap(_.agent.movingTo).exists(_.pixelDistance(pixelCenter) > 32)
    lazy val tryingToAttackHere = target.exists(t => t.isEnemyOf(this) &&  inRangeToAttack(t))
    lazy val tryingToAttackAway = target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    if ( ! moving && couldMove && (tryingToMove || tryingToAttackAway)) {
      framesFailingToMove += 1
    }
    else {
      framesFailingToMove = 0
    }
    if (tryingToAttackHere && cooldownLeft == 0) {
      framesFailingToAttack += 1
    }
    else {
      framesFailingToAttack = 0
    }
    if (lastFrameStartingAttack == thisFrame && target.nonEmpty) {
      With.damageCredit.onDamage(this, target.get)
    }
    if ( ! complete || completionFrame > 24 * 60 * 90) {
      completionFrame = With.frame + remainingBuildFrames
    }
    lastHitPoints             = hitPoints
    lastShieldPoints          = shieldPoints
    lastDefensiveMatrixPoints = defensiveMatrixPoints
    lastCooldown              = cooldownLeft
  }
  
  private lazy val stuckMoveFrames    = 10
  private lazy val stuckAttackFrames  = Math.max(stuckMoveFrames, cooldownMaxAirGround)
  def seeminglyStuck: Boolean = framesFailingToMove > stuckMoveFrames || framesFailingToAttack > stuckAttackFrames
  
  ////////////
  // Health //
  ////////////
  
  def aliveAndComplete:Boolean = alive && complete
  
  def energyMax     : Int = unitClass.maxEnergy //TODO: Add upgrades
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  ///////////////
  // Economics //
  ///////////////
  
  lazy val isMineralBlocker: Boolean = unitClass.isMinerals && mineralsLeft < With.configuration.blockerMineralThreshold
  
  def subjectiveValue: Int = (
    unitClass.subjectiveValue
    + scarabCount * Protoss.Scarab.subjectiveValue
    + interceptorCount * Protoss.Interceptor.subjectiveValue
    + friendly.map(_.loadedUnits.map(_.subjectiveValue).sum).sum
  )
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.x
  def y: Int = pixelCenter.y
  
  def left    = x - unitClass.dimensionLeft
  def right   = x + unitClass.dimensionRight
  def top     = y - unitClass.dimensionUp
  def bottom  = y + unitClass.dimensionDown
  
  def tileIncludingCenter:  Tile          = pixelCenter.tileIncluding
  def tileArea:             TileRectangle = unitClass.tileArea.add(tileTopLeft)
  def addonArea:            TileRectangle = TileRectangle(Tile(0, 0), Tile(2, 2)).add(tileTopLeft).add(4,1)
  
  def zone: Zone = cacheZone()
  private val cacheZone = new Cache(() => pixelCenter.zone)
  
  def base: Option[Base] = cacheBase()
  private val cacheBase = new Cache(() => pixelCenter.base)
  
  def mobilityForceGrid : AbstractGrid[Force]   = if (flying) With.grids.mobilityForceAir else With.grids.mobilityForceGround
  def mobilityGrid      : AbstractGrid[Int]     = if (flying) With.grids.mobilityAir else With.grids.mobilityGround
  def mobilityForce     : Force                 = mobilityForceGrid.get(tileIncludingCenter)
  def mobility          : Int                   = mobilityGrid.get(tileIncludingCenter)
  
  def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  def pixelRangeAir: Double = pixelRangeAirCache()
  private val pixelRangeAirCache = new Cache(() =>
    unitClass.pixelRangeAir +
      (if (isBunker())                                                  32.0 else 0.0) +
      (if (isBunker()     && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (isMarine()     && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (isGoliath()    && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (isDragoon()    && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (isHydralisk()  && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  
  def pixelRangeGround: Double = pixelRangeGroundCache()
  private val pixelRangeGroundCache = new Cache(() =>
    unitClass.pixelRangeGround +
      (if (isBunker())                                                32.0 else 0.0) +
      (if (isBunker()     && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (isMarine()     && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (isDragoon()    && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (isHydralisk()  && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  
  def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)
  
  def canTraverse(tile: Tile): Boolean = flying || With.grids.walkable.get(tile)
  
  def pixelStart                                            : Pixel   = Pixel(left, top)
  def pixelEnd                                              : Pixel   = Pixel(right, bottom)
  def pixelStartAt            (at: Pixel)                   : Pixel   = at.subtract(pixelCenter).add(left, top)
  def pixelEndAt              (at: Pixel)                   : Pixel   = at.subtract(pixelCenter).add(right, bottom)
  def pixelDistanceCenter     (otherPixel:  Pixel)          : Double  = pixelCenter.pixelDistance(otherPixel)
  def pixelDistanceCenter     (otherUnit:   UnitInfo)       : Double  = pixelDistanceCenter(otherUnit.pixelCenter)
  def pixelDistanceEdge       (other:       UnitInfo)       : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  def pixelDistanceEdge       (other: UnitInfo, at: Pixel)  : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  def pixelDistanceEdge       (oStart: Pixel, oEnd: Pixel)  : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, oStart, oEnd)
  def pixelDistanceEdge       (destination: Pixel)          : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, destination, destination)
  def pixelDistanceSquared    (otherUnit:   UnitInfo)       : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherPixel:  Pixel)          : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def pixelDistanceTravelling (destination: Pixel)          : Double  = pixelDistanceTravelling(pixelCenter, destination)
  def pixelDistanceTravelling (destination: Tile)           : Double  = pixelDistanceTravelling(pixelCenter, destination.pixelCenter)
  def pixelDistanceTravelling (from: Pixel, to: Pixel)      : Double  = if (flying) from.pixelDistance(to) else from.groundPixels(to)
  
  def velocity: Force = Force(velocityX, velocityY)
  
  def canMove: Boolean = canMoveCache()
  private val canMoveCache = new Cache(() =>
    (unitClass.canMove || (unitClass.isBuilding && flying))
    && unitClass.topSpeed > 0
    && canDoAnything
    && ! burrowed)
  
  def topSpeedChasing: Double = topSpeedChasingCache()
  private val topSpeedChasingCache = new Cache(() =>
    topSpeed
    * PurpleMath.nanToOne(
      Math.max(0, cooldownMaxAirGround - unitClass.stopFrames)
      / cooldownMaxAirGround.toDouble))
  
  def topSpeed: Double = topSpeedCache()
  private val topSpeedCache = new Cache(() =>
    if ( ! canMove) 0 else
      (if (ensnared) 0.5 else 1.0) * // TODO: Is this the multiplier?
      (if (stimmed) 1.5 else 1.0) * (
      unitClass.topSpeed * (if (
        (is(Terran.Vulture)   && player.hasUpgrade(Terran.VultureSpeed))    ||
        (is(Protoss.Observer) && player.hasUpgrade(Protoss.ObserverSpeed))  ||
        (is(Protoss.Scout)    && player.hasUpgrade(Protoss.ScoutSpeed))     ||
        (is(Protoss.Shuttle)  && player.hasUpgrade(Protoss.ShuttleSpeed))   ||
        (is(Protoss.Zealot)   && player.hasUpgrade(Protoss.ZealotSpeed))    ||
        (is(Zerg.Overlord)    && player.hasUpgrade(Zerg.ZerglingSpeed))     ||
        (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskSpeed))    ||
        (is(Zerg.Ultralisk)   && player.hasUpgrade(Zerg.UltraliskSpeed)))
        1.5 else 1.0)))
  
  def projectFrames(framesToLookAhead: Double): Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)
  
  def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  def isTransport: Boolean = unitClass.isTransport && ( ! is(Zerg.Overlord) || player.hasUpgrade(Zerg.OverlordDrops))
  
  def sightRangePixels: Int = sightRangePixelsCache()
  private val sightRangePixelsCache = new Cache(() =>
    if (blind) 32 else
    unitClass.sightRangePixels +
      (if (
        (is(Terran.Ghost)     && player.hasUpgrade(Terran.GhostVisionRange))      ||
        (is(Protoss.Observer) && player.hasUpgrade(Protoss.ObserverVisionRange))  ||
        (is(Protoss.Scout)    && player.hasUpgrade(Protoss.ScoutVisionRange))     ||
        (is(Zerg.Overlord)    && player.hasUpgrade(Zerg.OverlordVisionRange)))
      64 else 0))
  
  ////////////
  // Combat //
  ////////////
  
  def battle: Option[BattleLocal] = With.battles.byUnit.get(this).orElse(With.matchups.entrants.find(_._2.contains(this)).map(_._1))
  def matchups: MatchupAnalysis = With.matchups.get(this)
  
  def armorHealth: Int = armorHealthCache()
  def armorShield: Int = armorShieldsCache()
  
  lazy val armorHealthCache   = new Cache(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  lazy val armorShieldsCache  = new Cache(() => player.getUpgradeLevel(Protoss.Shields))
  
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  
  def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  
  def airDpf    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir.toDouble
  def groundDpf : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround.toDouble
  
  def attacksAgainstAir: Int = attacksAgainstAirCache()
  private val attacksAgainstAirCache = new Cache(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = interceptorCount
    output
  })
  
  def attacksAgainstGround: Int = attacksAgainstGroundCache()
  private val attacksAgainstGroundCache = new Cache(() => {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = interceptorCount
    if (output == 0  && is(Protoss.Reaver))   output = 1
    output
  })
  
  //TODO: Ensnare
  def cooldownLeft      : Int = Math.max(airCooldownLeft, groundCooldownLeft)
  def cooldownMaxAir    : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  def cooldownMaxGround : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG
  
  def cooldownMaxAirGround: Int = Math.max(
    if (unitClass.attacksAir)     cooldownMaxAir    else 0,
    if (unitClass.attacksGround)  cooldownMaxGround else 0)
  
  def cooldownMaxAgainst(enemy: UnitInfo): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround
  
  def pixelRangeAgainst(enemy: UnitInfo): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)
  
  def hitChanceAgainst(
    enemy : UnitInfo,
    from  : Option[Pixel] = None,
    to    : Option[Pixel] = None)
  : Double = {
    if (guaranteedToHit(enemy, from, to)) 1.0 else 0.47
  }
  
  def guaranteedToHit(
    enemy : UnitInfo,
    from  : Option[Pixel] = None,
    to    : Option[Pixel] = None)
  : Boolean = {
    val tileFrom  = from.getOrElse(pixelCenter).tileIncluding
    val tileTo    =   to.getOrElse(pixelCenter).tileIncluding
    (
      flying
      || enemy.flying
      || unitClass.unaffectedByDarkSwarm
      || With.grids.altitudeBonus.get(tileFrom) >= With.grids.altitudeBonus.get(tileTo)
    )
  }
  
  def damageTypeAgainst (enemy: UnitInfo)  : Damage.Type  = if (enemy.flying) unitClass.airDamageType    else unitClass.groundDamageType
  def attacksAgainst    (enemy: UnitInfo)  : Int          = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  
  def damageScaleAgainstHitPoints(enemy: UnitInfo): Double = {
    if (airDpf    <= 0 && enemy.flying)   return 0.0
    if (groundDpf <= 0&& ! enemy.flying)  return 0.0
    Damage.scaleBySize(damageTypeAgainst(enemy), enemy.unitClass.size)
  }
  
  def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  def damageOnHitGround   : Int = damageOnHitGroundCache()
  def damageOnHitAir      : Int = damageOnHitAirCache()
  def damageOnHitMax      : Int = Math.max(damageOnHitAir, damageOnHitGround)
  private val damageOnHitGroundCache  = new Cache(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new Cache(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  
  def damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  def damageOnNextHitAgainst(
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
  
  def dpfOnNextHitAgainst(enemy: UnitInfo): Double = {
    if (unitClass.suicides) {
      damageOnNextHitAgainst(enemy)
    }
    else {
      val cooldownVs = cooldownMaxAgainst(enemy)
      if (cooldownVs == 0)
        0.0
      else
        damageOnNextHitAgainst(enemy).toDouble / cooldownVs
    }
  }
  def vpfOnNextHitAgainst(enemy: UnitInfo): Double = dpfOnNextHitAgainst(enemy) / enemy.subjectiveValue
  
  def canDoAnything: Boolean = canDoAnythingCache()
  private val canDoAnythingCache = new Cache(() =>
    aliveAndComplete
    && ( ! unitClass.requiresPsi || powered)
    && ! stasised
    && ! maelstrommed
    && ! lockedDown)
  
  def canBeAttacked: Boolean = canBeAttackedCache()
  private val canBeAttackedCache = new Cache(() =>
      alive &&
      (complete || unitClass.isBuilding) &&
      totalHealth > 0 &&
      ! invincible &&
      ! stasised)
  
  def canAttack: Boolean = canAttackCache()
  private val canAttackCache = new Cache(() =>
    canDoAnything
    && ( ! unitClass.shootsScarabs || scarabCount > 0)
    && (
      unitClass.rawCanAttack
      || (is(Terran.Bunker)
      || (is(Protoss.Carrier) && interceptorCount > 0)
      || (is(Protoss.Reaver)  && scarabCount > 0)
      || (is(Zerg.Lurker)     && burrowed)
    ))
    && (flying || ! underDisruptionWeb))
  
  def canAttack(enemy: UnitInfo): Boolean = (
    canAttack
    && enemy.canBeAttacked
    && (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
    && ! enemy.effectivelyCloaked
    && ! friendly.exists(_.loaded)
    && (enemy.unitClass.triggersSpiderMines || ! isSpiderMine())
    && (unitClass.unaffectedByDarkSwarm || ! enemy.underDarkSwarm)
  )
  
  // Stupid, but helps performance
  protected class CacheIs(unitClass: UnitClass) extends Cache(() => is(unitClass))
  protected lazy val isSpiderMine         : CacheIs = new CacheIs(Terran.SpiderMine)
  protected lazy val isBunker             : CacheIs = new CacheIs(Terran.Bunker)
  protected lazy val isMarine             : CacheIs = new CacheIs(Terran.Marine)
  protected lazy val isGoliath            : CacheIs = new CacheIs(Terran.Goliath)
  protected lazy val isSiegeTankUnsieged  : CacheIs = new CacheIs(Terran.SiegeTankUnsieged)
  protected lazy val isDragoon            : CacheIs = new CacheIs(Protoss.Dragoon)
  protected lazy val isInterceptor        : CacheIs = new CacheIs(Protoss.Interceptor)
  protected lazy val isHydralisk          : CacheIs = new CacheIs(Zerg.Hydralisk)
  
  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  def framesToBeReadyForAttackOrder: Int = cooldownLeft - With.latency.framesRemaining - With.reaction.agencyMin
  def readyForAttackOrder: Boolean = canAttack && framesToBeReadyForAttackOrder <= 0
  
  def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  def pixelReachAgainst (framesAhead: Int, enemy:UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  
  def inRangeToAttack(enemy: UnitInfo)                    : Boolean = inRangeToAttack(enemy, enemy.pixelCenter)
  def inRangeToAttack(enemy: UnitInfo, targetAt: Pixel)   : Boolean = pixelDistanceEdge(enemy) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy, targetAt) > pixelRangeMin)
  def inRangeToAttack(enemy: UnitInfo, framesAhead: Int)  : Boolean = inRangeToAttack(enemy, enemy.projectFrames(framesAhead))
  
  def framesToTravelTo(destination: Pixel)  : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  def framesToTravelPixels(pixels: Double)  : Int = if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels/topSpeed).toInt) else Int.MaxValue
  
  def framesToGetInRange(enemy: UnitInfo)               : Int = framesToGetInRange(enemy, enemy.pixelCenter)
  def framesToGetInRange(enemy: UnitInfo, at: Pixel)    : Int = if (canAttack(enemy)) framesToTravelPixels(pixelDistanceEdge(enemy, at) - pixelRangeAgainst(enemy)) else Int.MaxValue
  def framesBeforeAttacking(enemy: UnitInfo)            : Int = framesBeforeAttacking(enemy, enemy.pixelCenter)
  def framesBeforeAttacking(enemy: UnitInfo, at: Pixel) : Int = {
    if (canAttack(enemy)) {
      Math.max(cooldownLeft, framesToGetInRange(enemy))
    }
    else Int.MaxValue
  }
  
  def canStim: Boolean = unitClass.canStim && player.hasTech(Terran.Stim) && hitPoints > 10
  
  def moving: Boolean = velocityX != 0 || velocityY != 0
  
  def speedApproaching(other: UnitInfo): Double = speedApproaching(other.pixelCenter)
  def speedApproaching(pixel: Pixel): Double = {
    val deltaXY = Force(x - pixel.x, y - pixel.y)
    val deltaV  = velocity
    val output  = - velocity.lengthFast * (deltaXY.normalize * velocity.normalize)
    output
  }
  
  ////////////
  // Orders //
  ////////////
  
  def gathering: Boolean = gatheringMinerals || gatheringGas
  
  def carryingResources: Boolean = carryingMinerals || carryingGas
  
  def isBeingViolent: Boolean = {
    unitClass.isStaticDefense ||
    attacking                 ||
    cooldownLeft > 0          ||
    target.exists(isEnemyOf)
  }
  
  def isBeingViolentTo(victim: UnitInfo): Boolean = {
    isBeingViolent &&
    //Are we capable of hurting the victim?
    isEnemyOf(victim) &&
    canAttack(victim) &&
    //Are we not attacking anyone else?
    ! target.exists(_ != victim) &&
    //Are we close to being able to hit the victim?
    framesToGetInRange(victim) < With.configuration.violenceThresholdFrames
  }
  
  ////////////////
  // Visibility //
  ////////////////
  
  def visibleToOpponents: Boolean =
    if (isFriendly)
      With.grids.enemyVision.isSet(tileIncludingCenter)
    else
      visible
  
  def likelyStillThere: Boolean =
    possiblyStillThere &&
    ( ! canMove || With.framesSince(lastSeen) < With.configuration.fogPositionDurationFrames || isSiegeTankUnsieged())
  
  def likelyStillAlive: Boolean =
    likelyStillThere      ||
    unitClass.isBuilding  ||
    unitClass.isWorker    ||
    With.framesSince(lastSeen) < (
      if (With.strategy.isFfa)
        GameTime(2, 0)()
      else
        GameTime(5, 0)()
    )
  
  def effectivelyCloaked: Boolean =
    (burrowed || cloaked) && (
      if (isFriendly)
        ! With.grids.enemyDetection.isSet(tileIncludingCenter)
        && (
          Math.min(With.framesSince(friendly.map(_.agent.lastCloak).getOrElse(0)), With.framesSince(lastFrameTakingDamage)) > 162 + 2
          || ! With.enemies.exists(_.isTerran))
          // Scanner sweep.
          // lasts 162 frames.
          // Cloakedness updates on per-unit timer lasting 30 frames.
          // There's also a global cloak timer every 300 frames.
      else            ! detected
    )
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player.isUs
  def isNeutral  : Boolean = player.isNeutral
  def isFriendly : Boolean = player.isAlly || isOurs
  def isEnemy    : Boolean = player.isEnemy
  def isEnemyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  def color: Color =
    if      (visible)             player.colorBright
    else if (likelyStillThere)    player.colorMedium
    else if (possiblyStillThere)  player.colorDark
    else if (likelyStillAlive)    player.colorMidnight
    else                          Colors.MidnightGray
}
