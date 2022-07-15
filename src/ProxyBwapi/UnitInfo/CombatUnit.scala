package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import Utilities.LightYear
import Utilities.Time.Forever

import scala.collection.mutable

/**
  * Shared base type for units which could either be real or simulated
  */
trait CombatUnit {
  def visibility                : Visibility.Value
  def player                    : PlayerInfo
  def unitClass                 : UnitClass
  def pixel                     : Pixel
  def visible                   : Boolean
  def alive                     : Boolean
  def complete                  : Boolean
  def burrowed                  : Boolean
  def cloaked                   : Boolean
  def detected                  : Boolean
  def flying                    : Boolean
  def plagued                   : Boolean
  def ensnared                  : Boolean
  def invincible                : Boolean
  def irradiated                : Boolean
  def lockedDown                : Boolean
  def maelstrommed              : Boolean
  def stasised                  : Boolean
  def stimmed                   : Boolean
  def hitPoints                 : Int
  def shieldPoints              : Int
  def matrixPoints              : Int
  def armorHealth               : Int
  def armorShield               : Int
  def cooldownLeft              : Int
  def loadedUnitCount           : Int
  def angleRadians              : Double
  def speed                     : Double
  def topSpeed                  : Double
  def topSpeedPossible          : Double

  def canMove: Boolean
  def canAttack(other: CombatUnit): Boolean

  @inline final def dead: Boolean = ! alive
  @inline final def attacksAgainstAir: Int = attacksAgainstAirCache()
  private val attacksAgainstAirCache = new Cache(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && unitClass == Terran.Bunker)   output = loadedUnitCount
    if (output == 0  && unitClass == Protoss.Carrier) output = loadedUnitCount
    output
  })
  @inline final def attacksAgainstGround: Int = attacksAgainstGroundCache()
  private val attacksAgainstGroundCache = new Cache(() => {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    if (output == 0  && unitClass == Terran.Bunker)   output = loadedUnitCount
    if (output == 0  && unitClass == Protoss.Carrier) output = loadedUnitCount
    if (output == 0  && unitClass == Protoss.Reaver)  output = 1
    output
  })
  @inline final def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  @inline final def pixelRangeAir: Double = pixelRangeAirCache()
  private val pixelRangeAirCache = new Cache(() =>
    unitClass.pixelRangeAir +
      (if (unitClass == Terran.Bunker)                                                32.0 else 0.0) +
      (if (unitClass == Terran.Bunker   && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (unitClass == Terran.Marine   && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (unitClass == Terran.Goliath  && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (unitClass == Protoss.Dragoon && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (unitClass == Zerg.Hydralisk  && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  @inline final def pixelRangeGround: Double = pixelRangeGroundCache()
  private val pixelRangeGroundCache = new Cache(() =>
    unitClass.pixelRangeGround +
      (if (unitClass == Terran.Bunker)                                              32.0 else 0.0) +
      (if (unitClass == Terran.Bunker   && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (unitClass == Terran.Marine   && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (unitClass == Protoss.Dragoon && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (unitClass == Zerg.Hydralisk  && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  @inline final def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  @inline final def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)

  @inline final def cooldownMaxAir    : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxGround : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxAirGround: Int = Math.max(if (unitClass.attacksAir) cooldownMaxAir else 0, if (unitClass.attacksGround)  cooldownMaxGround else 0)
  @inline final def cooldownMaxAgainst(enemy: CombatUnit): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround
  @inline final def pixelRangeAgainst(enemy: CombatUnit): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  @inline final def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)
  @inline final def formationRangePixels: Double = Math.max(0, Math.max(effectiveRangePixels, if (unitClass == Terran.SiegeTankUnsieged && Terran.SiegeMode(player)) Terran.SiegeTankSieged.effectiveRangePixels else 0) - (if (unitClass == Protoss.Reaver) 64 else 32))
  @inline final def totalHealth: Int = hitPoints + shieldPoints + matrixPoints

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
  @inline final def pixelStart                                              : Pixel   = Pixel(left, top)
  @inline final def pixelEnd                                                : Pixel   = Pixel(right, bottom)
  @inline final def pixelStartAt            (at: Pixel)                     : Pixel   = at.subtract(pixel).add(left, top)
  @inline final def pixelEndAt              (at: Pixel)                     : Pixel   = at.subtract(pixel).add(right, bottom)
  @inline final def pixelDistanceCenter     (otherPixel:  Pixel)            : Double  = pixel.pixelDistance(otherPixel)
  @inline final def pixelDistanceCenter     (otherUnit:   CombatUnit)       : Double  = pixelDistanceCenter(otherUnit.pixel)
  @inline final def pixelDistanceShooting   (other:       CombatUnit)       : Double  = if (unitClass == Protoss.Reaver && pixel.zone != other.pixel.zone) pixelDistanceTravelling(other.pixel) else pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  @inline final def pixelDistanceEdge       (other:       CombatUnit)       : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  @inline final def pixelDistanceEdge       (other: CombatUnit, otherAt: Pixel) : Double  = pixelDistanceEdge(otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(1 + other.unitClass.dimensionRight, 1 + other.unitClass.dimensionDown))
  @inline final def pixelDistanceEdge       (oStart: Pixel, oEnd: Pixel)    : Double  = Maff.broodWarDistanceBox(pixelStart, pixelEnd, oStart, oEnd)
  @inline final def pixelDistanceEdge       (destination: Pixel)            : Double  = Maff.broodWarDistanceBox(pixelStart, pixelEnd, destination, destination)
  @inline final def pixelDistanceEdge       (other: CombatUnit, usAt: Pixel, otherAt: Pixel): Double  = Maff.broodWarDistanceBox(usAt.subtract(unitClass.dimensionLeft, unitClass.dimensionUp), usAt.add(1 + unitClass.dimensionRight, 1 + unitClass.dimensionDown), otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRight, other.unitClass.dimensionDown))
  @inline final def pixelDistanceEdgeFrom   (other: CombatUnit, usAt: Pixel): Double  = other.pixelDistanceEdge(this, usAt)
  @inline final def pixelDistanceSquared    (otherUnit:   CombatUnit)       : Double  = pixelDistanceSquared(otherUnit.pixel)
  @inline final def pixelDistanceSquared    (otherPixel:  Pixel)            : Double  = pixel.pixelDistanceSquared(otherPixel)
  @inline final def pixelDistanceTravelling (destination: Pixel)            : Double  = pixelDistanceTravelling(pixel, destination)
  @inline final def pixelDistanceTravelling (destination: Tile)             : Double  = pixelDistanceTravelling(pixel, destination.center)
  @inline final def pixelDistanceTravelling (from: Pixel, to: Pixel)        : Double  = if (flying) from.pixelDistance(to) else from.walkableTile.groundPixels(to.walkableTile)
  @inline final def pixelDistanceTravelling (from: Tile,  to: Tile)         : Double  = if (flying) from.center.pixelDistance(to.center) else from.walkableTile.groundPixels(to.walkableTile)
  @inline final def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  @inline final def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  @inline final def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  @inline final def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  @inline final def pixelReachAgainst (framesAhead: Int, enemy: CombatUnit): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  @inline final def inRangeToAttack(enemy: CombatUnit)                    : Boolean = pixelDistanceEdge(enemy)          <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy)          > pixelRangeMin)
  @inline final def inRangeToAttack(enemy: UnitInfo, enemyAt: Pixel)      : Boolean = pixelDistanceEdge(enemy, enemyAt) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy, enemyAt) > pixelRangeMin)
  @inline final def inRangeToAttack(enemy: CombatUnit, usAt: Pixel, to: Pixel): Boolean = { val d = pixelDistanceEdge(enemy, usAt, to); d <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || d > pixelRangeMin) }
  @inline final def inRangeToAttackFrom(enemy: CombatUnit, usAt: Pixel)   : Boolean = pixelDistanceEdgeFrom(enemy, usAt) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdgeFrom(enemy, usAt) > pixelRangeMin)
  @inline final def pixelsToGetInRange(enemy: CombatUnit)                 : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy)          - pixelRangeAgainst(enemy)) else LightYear()
  @inline final def pixelsToGetInRange(enemy: CombatUnit, enemyAt: Pixel) : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy, enemyAt) - pixelRangeAgainst(enemy)) else LightYear()
  @inline final def pixelsToGetInRangeTraveling(enemy: CombatUnit)        : Double = Math.max(pixelsToGetInRange(enemy), if ( ! canAttack(enemy) || flying || inRangeToAttack(enemy)) 0 else pixelDistanceTravelling(enemy.pixel) - pixelRangeAgainst(enemy) - unitClass.dimensionMin - enemy.unitClass.dimensionMin)
  @inline final def framesToTravelTo(destination: Pixel)    : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  @inline final def framesToTravelTo(destination: Tile)     : Int = framesToTravelTo(destination.center)
  @inline final def framesToTravelPixels(pixels: Double)    : Int = (if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels / topSpeedPossible).toInt) else Forever()) + (if (burrowed || unitClass == Terran.SiegeTankSieged) 24 else 0)
  @inline final def framesToTurnTo    (radiansTo: Double)   : Double = unitClass.framesToTurn(Maff.normalizePiToPi(Maff.radiansTo(angleRadians, radiansTo)))
  @inline final def framesToTurnTo    (pixelTo: Pixel)      : Double = framesToTurnTo(pixel.radiansTo(pixelTo))
  @inline final def framesToTurnFrom  (pixelTo: Pixel)      : Double = framesToTurnTo(pixelTo.radiansTo(pixel))
  @inline final def framesToTurnTo    (enemyFrom: CombatUnit) : Double = framesToTurnTo(enemyFrom.pixel.radiansTo(pixel))
  @inline final def framesToTurnFrom  (enemyFrom: CombatUnit) : Double = framesToTurnTo(pixel.radiansTo(enemyFrom.pixel))
  @inline final def framesToStopRightNow: Double = if (unitClass.isFlyer || unitClass.floats) Maff.clamp(Maff.nanToZero(framesToAccelerate * speed / topSpeed), 0.0, framesToAccelerate) else 0.0
  @inline final def framesToAccelerate: Double = Maff.clamp(Maff.nanToZero((topSpeed - speed) / unitClass.accelerationFrames), 0, unitClass.accelerationFrames)
  @inline final def framesToGetInRange(enemy: CombatUnit)                 : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy)) else Forever()
  @inline final def framesToGetInRange(enemy: CombatUnit, enemyAt: Pixel) : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy, enemyAt)) else Forever()
  @inline final def framesBeforeAttacking(enemy: CombatUnit)              : Int = framesBeforeAttacking(enemy, enemy.pixel)
  @inline final def framesBeforeAttacking(enemy: CombatUnit, at: Pixel)   : Int = if (canAttack(enemy))  Math.max(cooldownLeft, framesToGetInRange(enemy)) else Forever()

  @inline final def hitChanceAgainst(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Double = if (guaranteedToHit(enemy, from, to)) 1.0 else 0.47
  @inline final def guaranteedToHit(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Boolean = {
    val tileFrom  = from.getOrElse(pixel)       .tile
    val tileTo    =   to.getOrElse(enemy.pixel) .tile
    flying || enemy.flying || unitClass.affectedByDarkSwarm || tileFrom.altitude >= tileTo.altitude
  }
  @inline final def damageTypeAgainst (enemy: CombatUnit)  : Damage.Type  = if (enemy.flying) unitClass.airDamageType    else unitClass.groundDamageType
  @inline final def attacksAgainst    (enemy: CombatUnit)  : Int          = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  @inline final def dpfOnNextHitAgainst(enemy: CombatUnit): Double = if (unitClass.suicides) damageOnNextHitAgainst(enemy) else { val cooldownVs = cooldownMaxAgainst(enemy); if (cooldownVs == 0) 0.0 else damageOnNextHitAgainst(enemy).toDouble / cooldownVs }
  @inline final def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  @inline final def damageOnHitGround   : Int = damageOnHitGroundCache()
  @inline final def damageOnHitAir      : Int = damageOnHitAirCache()
  private val damageOnHitGroundCache  = new Cache(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new Cache(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  @inline final def damageOnHitBeforeShieldsArmorAndDamageType(enemy: CombatUnit): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  @inline final def damageOnNextHitAgainst(enemy: CombatUnit, shields: Option[Int] = None, from: Option[Pixel] = None, to: Option[Pixel] = None): Int = {
    val enemyShieldPoints       = shields.getOrElse(enemy.shieldPoints)
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShieldPoints + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealthScale     = Damage.scaleBySize(damageTypeAgainst(enemy), enemy.unitClass.size)
    val damageToHealth          = Math.max(0.0, (damageAssignedToHealth - enemy.armorHealth * hits) * damageToHealthScale)
    val damageDealtTotal        = damageToHealth + damageToShields
    val hitChance               = hitChanceAgainst(enemy, from, to)
    val output                  = (hitChance * Math.max(1.0, damageDealtTotal)).toInt
    output
  }

  @inline final def isOurs     : Boolean = player.isUs
  @inline final def isNeutral  : Boolean = player.isNeutral
  @inline final def isFriendly : Boolean = player.isAlly || isOurs
  @inline final def isEnemy    : Boolean = player.isEnemy
  @inline final def isEnemyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
  @inline final def isAllyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isFriendly) || (isEnemy && otherUnit.isEnemy)

  ////////////
  // Damage //
  ////////////

  case class DamageSource(source: CombatUnit, onFrame: Int, committed: Boolean, guaranteed: Boolean, damageTotal: Int) {
    override def toString: String = f"$damageTotal from $source in ${With.framesUntil(onFrame)} frames ${if(guaranteed) "(Guaranteed)" else if (committed) "(Committed)" else ""}"
  }
  val damageQueue = new mutable.ArrayBuffer[DamageSource]()
  def addDamage(source: CombatUnit, inFrames: Int, committed: Boolean) {
    val damagePrevious = damageQueue.find(_.source == source)
    val damageNew = DamageSource(
      source      = source,
      onFrame     = Math.min(damagePrevious.map(_.onFrame).getOrElse(Forever()), With.frame + inFrames),
      committed   = damagePrevious.exists(_.committed) || committed,
      guaranteed  = damagePrevious.exists(_.guaranteed) || (committed && source.hitChanceAgainst(this) > .9),
      damageTotal = source.damageOnNextHitAgainst(this))
    damagePrevious.foreach(damageQueue.-=)
    val insertIndex = damageQueue.indexWhere(_.onFrame > damageNew.onFrame)
    if (insertIndex >= 0) damageQueue.insert(insertIndex, damageNew) else damageQueue += damageNew
  }
  def addFutureAttack(source: CombatUnit): Unit = {
    addDamage(
      source,
      source.framesToConnectDamage(this),
      committed = source.isOurs && source.inRangeToAttack(this))
  }
  def framesToConnectDamage(target: CombatUnit): Int = {
    // All of the math below here is approximate.
    // - Melee units without bullets (seem to) deal damage instantly
    // - Ranged units with instant-speed bullets (seem to) deal damage at the end of their attack animation
    // - Ranged units with projectile bullets deal damage after the bullet has arrived, and bullet travel time varies on bullet type/distance
    // As long as we don't overestimate the amount of damage actually done, and prefer underestimating time before attacks, we'll be okay
    Math.max(cooldownLeft, framesToGetInRange(target)) + expectedProjectileFrames(target)
  }
  /**
   * For the very small number of units with substantial stop frames
   * AND a travelling projectile,
   * this gives the expected duration in which bullet is present but target has yet to receive damage
   */
  def expectedProjectileFrames(target: CombatUnit): Int = {
    // At max range it looks to be 12 frames for a Dragoon.
    // Closer range can be 7 and possibly shorter
    if (unitClass == Protoss.Dragoon) unitClass.stopFrames + 7
    // Measured minimum of 5 frames for a Vulture right next to a Cannon
    else if (unitClass == Protoss.PhotonCannon) unitClass.stopFrames + 5
    // Measured 7 at close-ish range; assuming 6 is possible when closer
    else if (unitClass == Zerg.Mutalisk) 2
    // I definitely did not take the trouble to actually measure this
    else if (unitClass == Zerg.Devourer) unitClass.stopFrames + 7
    else 0
  }
  def removeDamage(source: CombatUnit): Unit = {
    val toRemove = damageQueue.view.filter(_.source == source).toVector
    damageQueue --= toRemove
  }
  def clearDamage() {
    damageQueue.clear()
  }
  def doomed: Boolean = doomFrameAbsolute < Forever()
  def doomedInFrames: Int = doomFrameAbsolute - With.frame
  def doomFrameAbsolute: Int = {
    var damageRequired = totalHealth
    var i = 0
    var frame = 0
    while (damageRequired > 0 && i < damageQueue.size) {
      val next: DamageSource = damageQueue(i)
      if (next.guaranteed) {
        damageRequired -= next.damageTotal
        frame = next.onFrame
        if (damageRequired <= 0) return frame
      }
      i += 1
    }
    Forever()
  }
  def likelyDoomed: Boolean = likelyDoomFrameAbsolute < Forever()
  def likelyDoomedInFrames: Int = likelyDoomFrameAbsolute - With.frame
  def likelyDoomFrameAbsolute: Int = {
    var damageRequired = totalHealth
    var i = 0
    var frame = 0
    while (damageRequired > 0 && i < damageQueue.size) {
      val next: DamageSource = damageQueue(i)
      damageRequired -= next.damageTotal
      frame = next.onFrame
      if (damageRequired <= 0) return frame
      i += 1
    }
    Forever()
  }
}
