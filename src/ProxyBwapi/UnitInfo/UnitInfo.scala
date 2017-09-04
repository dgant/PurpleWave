package ProxyBwapi.UnitInfo

import Information.Battles.Types.Battle
import Information.Geography.Types.{Base, Zone}
import Information.Grids.AbstractGrid
import Information.Kill
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.PurpleMath
import Micro.Matchups.MatchupAnalysis
import Performance.CacheFrame
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import Utilities.ByOption
import bwapi._

import scala.collection.mutable

abstract class UnitInfo(baseUnit: bwapi.Unit) extends UnitProxy(baseUnit) {
  
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
  
  def is(unitClasses: UnitClass*): Boolean = unitClasses.contains(unitClass)
  
  //////////////////
  // Statefulness //
  //////////////////
  
  def creditKill(kill: Kill) {
    kills += kill
  }
  
  val frameDiscovered   : Int = With.frame
  var frameChangedClass : Int = With.frame
  var completionFrame   : Int = Int.MaxValue // Can't use unitClass during construction
  
  var lastTarget    : Option[UnitInfo] = None
  var lastAttacker  : Option[UnitInfo] = None
  val kills         : mutable.ArrayBuffer[Kill] = new mutable.ArrayBuffer[Kill]

  private val history = new mutable.Queue[UnitState]
  def updateHistory() {
    // Save JNI overhead by not tracking history of Spider Mines and Interceptors
    if ( ! unitClass.orderable) return
    
    lastTarget = target.orElse(lastTarget)
    history.lastOption.foreach(lastState => {
      if (lastState.unitClass != unitClass) {
        frameChangedClass = With.frame
      }
      if (lastState.cooldown < cooldownLeft && lastState.attackTarget.nonEmpty) {
        val target = lastState.attackTarget.get
        target.lastAttacker = Some(this)
        With.damageCredit.onDamage(this, target)
      }
    })
    if ( ! complete) {
      completionFrame = With.frame + remainingBuildFrames
    }
    addHistory()
  }
  
  def addHistory() {
    // Don't stuff the queue while the game is paused
    if (history.lastOption.exists(_.frame == With.frame)) return
  
    while (history.headOption.exists(_.age > With.configuration.unitHistoryAge)) {
      history.dequeue()
    }
    
    history.enqueue(new UnitState(this))
  }
  
  def lastAttackStartFrame: Int = {
    val attackStartingStates = history.filter(_.attackStarting)
    if (attackStartingStates.isEmpty)
      0
    else
      attackStartingStates.map(_.frame).max
  }
  
  def lastMovementFrame: Int = {
    val movingFrames = history.filter(_.velocitySquared > 0)
    if (movingFrames.isEmpty)
      With.framesSince(frameDiscovered)
    else
      movingFrames.map(_.frame).max
  }
  
  def hasBeenViolentInLastTwoSeconds: Boolean = hasBeenViolentInLastTwoSecondsCache.get
  private val hasBeenViolentInLastTwoSecondsCache = new CacheFrame(() => history.exists(h => With.framesSince(h.frame) < 48 && h.cooldown > 0))
  
  def damageInLastSecond: Int = damageInLastSecondCache.get
  private val damageInLastSecondCache = new CacheFrame(() =>
    Math.max(
      0,
      history
        .filter(_.age > 24)
        .lastOption
        .map(lastState =>
          lastState.hitPoints             - hitPoints     +
          lastState.shieldPoints          - shieldPoints  +
          lastState.defensiveMatrixPoints - defensiveMatrixPoints)
        .getOrElse(0)))
  
  private lazy val stuckMoveFrames  = 12
  private lazy val stuckAttackFrames = cooldownMaxAirGround + 8
  private lazy val stuckFramesMax    = Math.max(stuckMoveFrames, stuckAttackFrames)
  def seeminglyStuck: Boolean = {
    val recentHistory = history.takeRight(stuckFramesMax)
    history.size >= stuckFramesMax && (
      history.takeRight(stuckMoveFrames   ).forall(state => state.couldMoveThisFrame    && state.tryingToMove   && state.pixelCenter == pixelCenter) ||
      history.takeRight(stuckAttackFrames ).forall(state => state.couldAttackThisFrame  && state.tryingToAttack && state.pixelCenter == pixelCenter && state.cooldown == 0)
    )
  }
  
  ////////////
  // Health //
  ////////////
  
  def aliveAndComplete:Boolean = alive && complete
  
  def energyMax     : Int = unitClass.maxEnergy //TODO: Add upgrades
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  def wounded: Boolean = totalHealth < Math.min(With.configuration.woundedThresholdHealth, unitClass.maxTotalHealth / 3)
  
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
  
  def tileIncludingCenter:  Tile          = pixelCenter.tileIncluding
  def tileArea:             TileRectangle = unitClass.tileArea.add(tileTopLeft)
  def addonArea:            TileRectangle = TileRectangle(Tile(0, 0), Tile(2, 2)).add(tileTopLeft).add(4,1)
  
  def zone: Zone = cacheZone.get
  private val cacheZone = new CacheFrame(() => pixelCenter.zone)
  
  def base: Option[Base] = cacheBase.get
  private val cacheBase = new CacheFrame(() => ByOption.minBy(zone.bases)(_.heart.tileDistanceFast(tileIncludingCenter)))
  
  def mobilityForceGrid : AbstractGrid[Force]   = if (flying) With.grids.mobilityForceAir else With.grids.mobilityForceGround
  def mobilityGrid      : AbstractGrid[Int]     = if (flying) With.grids.mobilityAir else With.grids.mobilityGround
  def mobilityForce     : Force                 = mobilityForceGrid.get(tileIncludingCenter)
  def mobility          : Int                   = mobilityGrid.get(tileIncludingCenter)
  
  def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  def pixelRangeAir: Double = pixelRangeAirCache.get
  private val pixelRangeAirCache = new CacheFrame(() =>
    unitClass.airRangePixels +
      (if (is(Terran.Bunker))                                                 32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Goliath)   && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  
  def pixelRangeGround: Double = pixelRangeGroundCache.get
  private val pixelRangeGroundCache = new CacheFrame(() =>
    unitClass.groundRangePixels +
      (if (is(Terran.Bunker))                                               32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  
  def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)
  
  def canTraverse             (tile:        Tile)       : Boolean = flying || With.grids.walkable.get(tile)
  def pixelsFromEdgeSlow      (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelsFromEdgeFast      (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelDistanceSlow       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSlow(otherPixel)
  def pixelDistanceSlow       (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit.pixelCenter)
  def pixelDistanceFast       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceFast(otherPixel)
  def pixelDistanceFast       (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherUnit:   UnitInfo)   : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def pixelDistanceTravelling (destination: Pixel)      : Double  = pixelDistanceTravelling(pixelCenter, destination)
  def pixelDistanceTravelling (destination: Tile)       : Double  = pixelDistanceTravelling(pixelCenter, destination.pixelCenter)
  def pixelDistanceTravelling (from: Pixel, to: Pixel)  : Double  = if (flying) from.pixelDistanceFast(to) else from.groundPixels(to)
  
  def velocity: Force = Force(velocityX, velocityY)
  
  def canMove: Boolean = canMoveCache.get
  private val canMoveCache = new CacheFrame(() => unitClass.canMove && topSpeed > 0 && canDoAnything && ! burrowed)
  
  def topSpeedChasing: Double = topSpeedChasingCache.get
  private val topSpeedChasingCache = new CacheFrame(() => topSpeed * PurpleMath.nanToOne(Math.max(0, cooldownMaxAirGround - unitClass.stopFrames) / cooldownMaxAirGround.toDouble))
  
  def topSpeed: Double = topSpeedCache.get
  //TODO: Ensnare
  private val topSpeedCache = new CacheFrame(() =>
    if ( ! canDoAnything || burrowed) 0 else
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
  
  
  
  // TODO. We need better math :( BWAPI doesn't define what its accel/turn rate numbers mean.
  // https://github.com/bwapi/bwapi/blob/59b14af21b3c881ce06af8b1ea1d63fa3c8b2df0/bwapi/include/BWAPI/UnitType.h#L555
  def framesToTurnAndShootAndTurnBackAndAccelerate: Int = unitClass.minStop + unitClass.stopFrames + 24
  
  def projectFrames(framesToLookAhead: Int): Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)
  
  def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  def isTransport: Boolean = unitClass.isTransport && ( ! is(Zerg.Overlord) || player.hasUpgrade(Zerg.OverlordDrops))
  
  def sightRangePixels: Int = sightRangePixelsCache.get
  private val sightRangePixelsCache = new CacheFrame(() =>
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
  
  def battle: Option[Battle] = With.battles.byUnit.get(this)
  def matchups: MatchupAnalysis = With.matchups.get(this)
  
  def ranged  : Boolean = unitClass.rawCanAttack && unitClass.maxAirGroundRangePixels > 32 * 2
  def melee   : Boolean = unitClass.rawCanAttack && ! ranged
  
  def armorHealth: Int = armorHealthCache.get
  def armorShield: Int = armorShieldsCache.get
  
  lazy val armorHealthCache   = new CacheFrame(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  lazy val armorShieldsCache  = new CacheFrame(() => player.getUpgradeLevel(Protoss.Shields))
  
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  
  def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  
  def airDpf    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir
  def groundDpf : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround
  
  def attacksAgainstAir: Int = attacksAgainstAirCache.get
  private val attacksAgainstAirCache = new CacheFrame(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && is(Terran.Bunker))    output = 4
    if (output == 0  && is(Protoss.Carrier))  output = interceptorCount
    output
  })
  
  def attacksAgainstGround: Int = attacksAgainstGroundCache.get
  private val attacksAgainstGroundCache = new CacheFrame(() => {
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
  
  def pixelRangeAgainstFromEdge   (enemy: UnitInfo): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  def pixelRangeAgainstFromCenter (enemy: UnitInfo): Double = pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse
  def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)
  
  def missChanceAgainst(enemy: UnitInfo): Double = {
    if (guaranteedToHit(enemy)) 0.53 else 0.0
  }
  def guaranteedToHit(enemy: UnitInfo): Boolean =
    flying                          ||
    enemy.flying                    ||
    unitClass.unaffectedByDarkSwarm ||
    With.grids.altitudeBonus.get(tileIncludingCenter) >= With.grids.altitudeBonus.get(enemy.tileIncludingCenter)
  
  def damageTypeAgainst (enemy: UnitInfo)  : DamageType  = if (enemy.flying) unitClass.airDamageTypeRaw else unitClass.groundDamageTypeRaw
  def attacksAgainst    (enemy: UnitInfo)  : Int         = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  
  def damageScaleAgainstHitPoints(enemy: UnitInfo): Double =
    if (enemy.flying && airDpf > 0)
      Damage.scaleBySize(unitClass.airDamageTypeRaw, enemy.unitClass.size)
    else if (groundDpf > 0)
      Damage.scaleBySize(unitClass.groundDamageTypeRaw, enemy.unitClass.size)
    else
      0.0
  
  def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  def damageOnHitGround   : Int = damageOnHitGroundCache.get
  def damageOnHitAir      : Int = damageOnHitAirCache.get
  def damageOnHitMax      : Int = Math.max(damageOnHitAir, damageOnHitGround)
  private val damageOnHitGroundCache  = new CacheFrame(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new CacheFrame(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  
  def damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  def damageOnNextHitAgainst(enemy: UnitInfo): Int = {
    damageOnNextHitAgainst(enemy, enemy.shieldPoints)
  }
  
  def damageOnNextHitAgainst(enemy: UnitInfo, enemyShields: Int): Int = {
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo)
    val damageScale             = damageScaleAgainstHitPoints(enemy)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShields + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealth          = (damageAssignedToHealth - enemy.armorHealth * hits) * damageScaleAgainstHitPoints(enemy)
    val damageDealtTotal        = damageAssignedToHealth + damageAssignedToShields
    Math.max(1, missChanceAgainst(enemy) * damageDealtTotal).toInt
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
  
  def canDoAnything: Boolean = canDoAnythingCache.get
  private val canDoAnythingCache = new CacheFrame(() =>
    aliveAndComplete  &&
    ( ! unitClass.requiresPsi || powered) &&
    ! stasised        && // These three checks along comprise 6% of our CPU usage. Yes, really.
    ! maelstrommed    &&
    ! lockedDown)
  
  def canBeAttacked: Boolean = canBeAttackedCache.get
  private val canBeAttackedCache = new CacheFrame(() =>
      alive &&
      (complete || unitClass.isBuilding) &&
      totalHealth > 0 &&
      ! invincible &&
      ! stasised)
  
  def canAttack: Boolean = canAttackCache.get
  private val canAttackCache = new CacheFrame(() =>
    canDoAnything &&
    ( ! unitClass.shootsScarabs || scarabCount > 0) &&
    (
      unitClass.rawCanAttack
      || (is(Terran.Bunker)
      || (is(Protoss.Carrier) && interceptorCount > 0)
      || (is(Protoss.Reaver)  && scarabCount > 0)
      || (is(Zerg.Lurker)     && burrowed)
    )))
  
  def canAttack(enemy: UnitInfo): Boolean =
    canAttack                             &&
    enemy.canBeAttacked                   &&
    ! enemy.effectivelyCloaked            && // Eh.
    ! enemy.friendly.exists(_.transport.isDefined) &&
    (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround) &&
    ( ! enemy.unitClass.floats || ! unitClass.suicides || ! is(Terran.SpiderMine))
  
  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  
  def readyForAttackOrder: Boolean = canAttack && cooldownLeft <= 1 + With.latency.framesRemaining
  
  def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  def pixelReachAgainst (framesAhead: Int, enemy:UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  
  def inRangeToAttackSlow(enemy: UnitInfo)                        : Boolean = pixelsFromEdgeSlow(enemy) <= pixelRangeAgainstFromEdge(enemy)     + With.configuration.attackableRangeBuffer && (pixelRangeMin <= 0.0 || pixelsFromEdgeSlow(enemy) > pixelRangeMin)
  def inRangeToAttackFast(enemy: UnitInfo)                        : Boolean = pixelsFromEdgeFast(enemy) <= pixelRangeAgainstFromEdge(enemy)     + With.configuration.attackableRangeBuffer && (pixelRangeMin <= 0.0 || pixelsFromEdgeFast(enemy) > pixelRangeMin)
  def inRangeToAttackSlow(enemy: UnitInfo, framesAhead  : Int)    : Boolean = enemy.projectFrames(framesAhead).pixelDistanceSlow(projectFrames(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer //TODO: Needs min range!
  def inRangeToAttackFast(enemy: UnitInfo, framesAhead  : Int)    : Boolean = enemy.projectFrames(framesAhead).pixelDistanceFast(projectFrames(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer //TODO: Needs min range!
  
  def framesToTravelTo(destination: Pixel)  : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  def framesToTravelPixels(pixels: Double)  : Int = if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels/topSpeed).toInt) else Int.MaxValue
  
  def framesToGetInRange(enemy: UnitInfo)               : Int = framesToGetInRange(enemy, enemy.pixelCenter)
  def framesToGetInRange(enemy: UnitInfo, at: Pixel)    : Int = if (canAttack(enemy)) framesToTravelPixels(pixelDistanceFast(at) - pixelRangeAgainstFromCenter(enemy)) else Int.MaxValue
  def framesBeforeAttacking(enemy: UnitInfo)            : Int = framesBeforeAttacking(enemy, enemy.pixelCenter)
  def framesBeforeAttacking(enemy: UnitInfo, at: Pixel) : Int = {
    if (canAttack(enemy)) {
      Math.max(cooldownLeft, framesToGetInRange(enemy))
    }
    else Int.MaxValue
  }
  
  def canStim: Boolean = (is(Terran.Marine) || is(Terran.Firebat)) && player.hasTech(Terran.Stim) && hitPoints > 10
  
  def moving: Boolean = velocityX != 0 || velocityY != 0
  
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
    framesToGetInRange(victim) < With.configuration.violenceFrameThreshold
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
    ( ! canMove || With.framesSince(lastSeen) < With.configuration.fogPositionDuration || is(Terran.SiegeTankUnsieged))
  
  def effectivelyCloaked: Boolean =
    (burrowed || cloaked) && (
      if (isFriendly) ! With.grids.enemyDetection.isSet(tileIncludingCenter) && damageInLastSecond == 0
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
    else                          player.colorMidnight
}
