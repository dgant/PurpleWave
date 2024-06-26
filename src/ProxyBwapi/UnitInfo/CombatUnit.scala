package ProxyBwapi.UnitInfo

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import ProxyBwapi.Damage
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import Utilities.{?, LightYear}
import Utilities.Time.Forever

import scala.collection.mutable
import scala.util.Try

/**
  * Shared base type for units which could either be real or simulated
  */
trait CombatUnit {
  def unitInfo: Option[UnitInfo] = Try(asInstanceOf[UnitInfo]).toOption

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
  def targetValue               : Double
  def injury                    : Double

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
  @inline final def stimAttackSpeedBonus: Int = ?(stimmed, 2, 1)
  @inline final def pixelRangeAir: Double = pixelRangeAirCache()
  private val pixelRangeAirCache = new Cache(() =>
    unitClass.pixelRangeAir +
      ?(unitClass == Terran.Bunker   && player.hasUpgrade(Terran.MarineRange),     32.0, 0.0) +
      ?(unitClass == Terran.Marine   && player.hasUpgrade(Terran.MarineRange),     32.0, 0.0) +
      ?(unitClass == Terran.Goliath  && player.hasUpgrade(Terran.GoliathAirRange), 96.0, 0.0) +
      ?(unitClass == Protoss.Dragoon && player.hasUpgrade(Protoss.DragoonRange),   64.0, 0.0) +
      ?(unitClass == Zerg.Hydralisk  && player.hasUpgrade(Zerg.HydraliskRange),    32.0, 0.0))
  @inline final def pixelRangeGround: Double = pixelRangeGroundCache()
  private val pixelRangeGroundCache = new Cache(() =>
    unitClass.pixelRangeGround +
      ?(unitClass == Terran.Bunker   && player.hasUpgrade(Terran.MarineRange),   32.0, 0.0) +
      ?(unitClass == Terran.Marine   && player.hasUpgrade(Terran.MarineRange),   32.0, 0.0) +
      ?(unitClass == Protoss.Dragoon && player.hasUpgrade(Protoss.DragoonRange), 64.0, 0.0) +
      ?(unitClass == Zerg.Hydralisk  && player.hasUpgrade(Zerg.HydraliskRange),  32.0, 0.0))
  @inline final def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  @inline final def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)

  @inline final def totalHealth         : Int = hitPoints + shieldPoints + matrixPoints
  @inline final def cooldownMaxAir      : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxGround   : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG
  @inline final def cooldownMaxAirGround: Int = Math.max(?(unitClass.attacksAir, cooldownMaxAir, 0), ?(unitClass.attacksGround, cooldownMaxGround, 0))
  @inline final def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)

  @inline final def x                     : Int   = pixel.x
  @inline final def y                     : Int   = pixel.y
  @inline final def left                  : Int   = x - unitClass.dimensionLeft
  @inline final def rightInclusive        : Int   = x + unitClass.dimensionRightInclusive
  @inline final def rightExclusive        : Int   = rightInclusive + 1
  @inline final def top                   : Int   = y - unitClass.dimensionUp
  @inline final def bottomInclusive       : Int   = y + unitClass.dimensionDownInclusive
  @inline final def bottomExclusive       : Int   = bottomInclusive + 1
  @inline final def topLeft               : Pixel = Pixel(left, top)
  @inline final def topRightInclusive     : Pixel = Pixel(rightInclusive, top)
  @inline final def topRightExclusive     : Pixel = Pixel(rightExclusive, top)
  @inline final def bottomLeftInclusive   : Pixel = Pixel(left, bottomInclusive)
  @inline final def bottomLeftExclusive   : Pixel = Pixel(left, bottomExclusive)
  @inline final def bottomRightInclusive  : Pixel = Pixel(rightInclusive, bottomInclusive)
  @inline final def bottomRightExclusive  : Pixel = Pixel(rightExclusive, bottomExclusive)

  @inline final def cornersInclusive: Vector[Pixel] = Vector(topLeft, topRightInclusive, bottomLeftInclusive, bottomRightInclusive)

  @inline final def pixelStart                                                                        : Pixel   = Pixel(left, top)
  @inline final def pixelEndExclusive                                                                 : Pixel   = Pixel(rightExclusive, bottomExclusive)
  @inline final def pixelStartAt                  (at: Pixel)                                         : Pixel   = at.subtract(pixel).add(left, top)
  @inline final def pixelEndAtExclusive           (at: Pixel)                                         : Pixel   = at.subtract(pixel).add(rightExclusive, bottomExclusive)
  @inline final def pixelDistanceCenter           (to: Pixel)                                         : Double  = pixel.pixelDistance(to)
  @inline final def pixelDistanceCenter           (other: CombatUnit)                                 : Double  = pixelDistanceCenter(other.pixel)
  @inline final def pixelDistanceShooting         (other: CombatUnit)                                 : Double  = ?(unitClass == Protoss.Reaver && pixel.zone != other.pixel.zone, pixelDistanceTravelling(other.pixel), pixelDistanceEdge(other.pixelStart, other.pixelEndExclusive))
  @inline final def pixelDistanceEdge             (boxStart: Pixel, boxEnd: Pixel)                    : Double  = Maff.broodWarDistanceBox(pixelStart, pixelEndExclusive, boxStart, boxEnd)
  @inline final def pixelDistanceEdge             (destination: Pixel)                                : Double  = Maff.broodWarDistanceBox(pixelStart, pixelEndExclusive, destination, destination)
  @inline final def pixelDistanceEdge             (other: CombatUnit)                                 : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEndExclusive)
  @inline final def pixelDistanceEdge             (other: CombatUnit,              otherAt: Pixel)    : Double  = pixelDistanceEdge(otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRightExclusive, other.unitClass.dimensionDownExclusive))
  @inline final def pixelDistanceEdge             (other: CombatUnit, usAt: Pixel, otherAt: Pixel)    : Double  = Maff.broodWarDistanceBox(usAt.subtract(unitClass.dimensionLeft, unitClass.dimensionUp), usAt.add(unitClass.dimensionRightExclusive, unitClass.dimensionDownExclusive), otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRightExclusive, other.unitClass.dimensionDownExclusive))
  @inline final def pixelDistanceEdgeFrom         (other: CombatUnit, usAt: Pixel)                    : Double  = other.pixelDistanceEdge(this, usAt)
  @inline final def pixelDistanceSquared          (other: CombatUnit)                                 : Double  = pixelDistanceSquared(other.pixel)
  @inline final def pixelDistanceSquared          (to: Pixel)                                         : Double  = pixel.pixelDistanceSquared(to)
  @inline final def pixelDistanceTravelling       (to: Pixel)                                         : Double  = pixelDistanceTravelling(pixel, to)
  @inline final def pixelDistanceTravelling       (to: Tile)                                          : Double  = pixelDistanceTravelling(pixel, to.center)
  @inline final def pixelDistanceTravelling       (from: Pixel, to: Pixel)                            : Double  = ?(flying, from.pixelDistance(to),               from.walkableTile.groundPixels(to.walkableTile))
  @inline final def pixelDistanceTravelling       (from: Tile,  to: Tile)                             : Double  = ?(flying, from.center.pixelDistance(to.center), from.walkableTile.groundPixels(to.walkableTile))
  @inline final def inRangeToAttack               (target: CombatUnit)                                : Boolean = pixelDistanceEdge(target)           <= pixelRangeAgainst(target) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(target)           > pixelRangeMin)
  @inline final def inRangeToAttack               (target: CombatUnit,              targetAt: Pixel)  : Boolean = pixelDistanceEdge(target, targetAt) <= pixelRangeAgainst(target) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(target, targetAt) > pixelRangeMin)
  @inline final def inRangeToAttack               (target: CombatUnit, from: Pixel, targetAt: Pixel)  : Boolean = { val d = pixelDistanceEdge(target, from, targetAt); d <= pixelRangeAgainst(target) && (pixelRangeMin <= 0.0 || d > pixelRangeMin) }
  @inline final def inRangeToAttackFrom           (target: CombatUnit, from: Pixel)                   : Boolean = pixelDistanceEdgeFrom(target, from) <= pixelRangeAgainst(target) && (pixelRangeMin <= 0.0 || pixelDistanceEdgeFrom(target, from) > pixelRangeMin)
  @inline final def pixelsToGetInRange            (target: CombatUnit)                                : Double  = ?(canAttack(target), pixelDistanceEdge(target)             - pixelRangeAgainst(target), LightYear())
  @inline final def pixelsToGetInRange            (target: CombatUnit,              targetAt: Pixel)  : Double  = ?(canAttack(target), pixelDistanceEdge(target, targetAt)   - pixelRangeAgainst(target), LightYear())
  @inline final def pixelsToGetInRangeFrom        (target: CombatUnit, from: Pixel)                   : Double  = ?(canAttack(target), pixelDistanceEdgeFrom(target, from)   - pixelRangeAgainst(target), LightYear())
  @inline final def pixelsToGetInRangeTraveling   (target: CombatUnit)                                : Double  = Math.max(pixelsToGetInRange(target), ?( ! canAttack(target) || flying || inRangeToAttack(target), 0, pixelDistanceTravelling(target.pixel) - pixelRangeAgainst(target) - unitClass.dimensionMin - target.unitClass.dimensionMin))
  @inline final def framesToTravelTo              (destination: Pixel)                                : Int     = framesToTravelPixels(pixelDistanceTravelling(destination))
  @inline final def framesToTravelTo              (destination: Tile)                                 : Int     = framesToTravelTo(destination.center)
  @inline final def framesToTravelPixels          (pixels: Double)                                    : Int     = (if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels / topSpeedPossible).toInt) else Forever()) + ?(burrowed || unitClass == Terran.SiegeTankSieged, 24, 0)
  @inline final def framesToTurnTo                (radiansTo: Double)                                 : Double  = unitClass.framesToTurn(Maff.normalizePiToPi(Maff.radiansTo(angleRadians, radiansTo)))
  @inline final def framesToTurnTo                (pixelTo: Pixel)                                    : Double  = framesToTurnTo(pixel.radiansTo(pixelTo))
  @inline final def framesToTurnFrom              (pixelTo: Pixel)                                    : Double  = framesToTurnTo(pixelTo.radiansTo(pixel))
  @inline final def framesToTurnTo                (other: CombatUnit)                                 : Double  = framesToTurnTo(other.pixel.radiansTo(pixel))
  @inline final def framesToTurnFrom              (other: CombatUnit)                                 : Double  = framesToTurnTo(pixel.radiansTo(other.pixel))
  @inline final def framesToStopRightNow                                                              : Double  = ?(unitClass.isFlyer || unitClass.floats, Maff.clamp(Maff.nanToZero(framesToAccelerate * speed / topSpeed), 0.0, framesToAccelerate), 0.0)
  @inline final def framesToAccelerate                                                                : Double  = Maff.clamp(Maff.nanToZero((topSpeed - speed) / unitClass.accelerationFrames), 0, unitClass.accelerationFrames)
  @inline final def framesToGetInRange            (enemy: CombatUnit)                                 : Int     = ?(canAttack(enemy), framesToTravelPixels(pixelsToGetInRange(enemy)),          Forever())
  @inline final def framesToGetInRange            (enemy: CombatUnit, enemyAt: Pixel)                 : Int     = ?(canAttack(enemy), framesToTravelPixels(pixelsToGetInRange(enemy, enemyAt)), Forever())
  @inline final def framesBeforeAttacking         (enemy: CombatUnit)                                 : Int     = framesBeforeAttacking(enemy, enemy.pixel)
  @inline final def framesBeforeAttacking         (enemy: CombatUnit, at: Pixel)                      : Int     = ?(canAttack(enemy),  Math.max(cooldownLeft, framesToGetInRange(enemy)), Forever())
  @inline final def attacksAgainst                (enemy: CombatUnit)                                 : Int     = ?(enemy.flying, attacksAgainstAir, attacksAgainstGround)
  @inline final def cooldownMaxAgainst            (enemy: CombatUnit)                                 : Int     = ?(enemy.flying, cooldownMaxAir, cooldownMaxGround)
  @inline final def pixelRangeAgainst             (enemy: CombatUnit)                                 : Double  = ?(enemy.flying, pixelRangeAir,  pixelRangeGround)
  @inline final def damageMultiplierAgainst       (enemy: CombatUnit)                                 : Double  = damageTypeAgainst(enemy)(enemy.unitClass.size)
  @inline final def dpfOnNextHitAgainst           (enemy: CombatUnit)                                 : Double  = ?(unitClass.suicides, damageOnNextHitAgainst(enemy), Math.max(0.0, Maff.nanToZero(damageOnNextHitAgainst(enemy).toDouble / cooldownMaxAgainst(enemy))))
  @inline final def damageTypeAgainst             (enemy: CombatUnit)                                 : Damage.Type = ?(enemy.flying, unitClass.airDamageType, unitClass.groundDamageType)

  @inline final def guaranteedToHit(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Boolean = {
    flying || enemy.flying || unitClass.affectedByDarkSwarm || from.getOrElse(pixel).tile.altitude >= to.getOrElse(enemy.pixel).tile.altitude
  }
  @inline final def hitChanceAgainst(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Double = {
    ?(guaranteedToHit(enemy, from, to), 1.0, 0.47)
  }
  @inline final def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  @inline final def damageOnHitGround   : Int = damageOnHitGroundCache()
  @inline final def damageOnHitAir      : Int = damageOnHitAirCache()
  private val damageOnHitGroundCache  = new Cache(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new Cache(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  @inline final def damageOnHitBeforeShieldsArmorAndDamageType(enemy: CombatUnit): Int = ?(enemy.flying, damageOnHitAir, damageOnHitGround)
  @inline final def damageOnNextHitAgainst(enemy: CombatUnit, shields: Option[Int] = None, from: Option[Pixel] = None, to: Option[Pixel] = None): Int = {
    val enemyShieldPoints       = shields.getOrElse(enemy.shieldPoints)
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShieldPoints + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealthScale     = damageTypeAgainst(enemy)(enemy.unitClass.size)
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
  @inline final def isEnemyOf (otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy)    || (isEnemy && otherUnit.isFriendly)
  @inline final def isAllyOf  (otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isFriendly) || (isEnemy && otherUnit.isEnemy)

  ////////////
  // Damage //
  ////////////

  case class DamageSource(source: CombatUnit, onFrame: Int, committed: Boolean, guaranteed: Boolean, damageTotal: Int) {
    override def toString: String = f"$damageTotal from $source in ${With.framesUntil(onFrame)} frames ${if(guaranteed) "(Guaranteed)" else if (committed) "(Committed)" else ""}"
  }
  val damageQueue = new mutable.ArrayBuffer[DamageSource]()
  def addDamage(source: CombatUnit, inFrames: Int, committed: Boolean, automaticallyGuaranteed: Boolean, fixedDamage: Option[Int] = None): Unit = {
    val damagePrevious = damageQueue.find(_.source == source)
    val damageNew = DamageSource(
      source      = source,
      onFrame     = Math.min(damagePrevious.map(_.onFrame).getOrElse(Forever()), With.frame + inFrames),
      committed   = automaticallyGuaranteed || damagePrevious.exists(_.committed)   || committed,
      guaranteed  = automaticallyGuaranteed || damagePrevious.exists(_.guaranteed)  || (committed && source.hitChanceAgainst(this) > .9),
      damageTotal = fixedDamage.getOrElse(source.damageOnNextHitAgainst(this)))
    damagePrevious.foreach(damageQueue.-=)
    val insertIndex = damageQueue.indexWhere(_.onFrame > damageNew.onFrame)
    ?(insertIndex >= 0, damageQueue.insert(insertIndex, damageNew), damageQueue += damageNew)
  }
  def addFutureAttack(source: CombatUnit): Unit = {
    addDamage(
      source,
      source.framesToConnectDamage(this),
      committed = source.isOurs && source.inRangeToAttack(this),
      automaticallyGuaranteed = false)
  }
  def framesToLaunchAttack(target: CombatUnit): Int = {
    Maff.vmax(unitInfo.map(_.remainingOccupationFrames).getOrElse(0), cooldownLeft, framesToGetInRange(target))
  }
  def framesToConnectDamage(target: CombatUnit): Int = {
    // All of the math below here is approximate.
    // - Melee units without bullets (seem to) deal damage instantly
    // - Ranged units with instant-speed bullets (seem to) deal damage at the end of their attack animation
    // - Ranged units with projectile bullets deal damage after the bullet has arrived, and bullet travel time varies on bullet type/distance
    // As long as we don't overestimate the amount of damage actually done, and prefer underestimating time before attacks, we'll be okay.
    framesToLaunchAttack(target) + expectedProjectileFrames(target)
  }
  def expectedProjectileFrames(target: CombatUnit): Int = {
    // At no point have I attempted to figure out bullet duration as a function of target distance
    unitClass.expectedProjectileFrames
  }
  def removeDamage(source: CombatUnit): Unit = {
    val toRemove = damageQueue.view.filter(_.source == source).toVector
    damageQueue --= toRemove
  }
  def clearDamage(): Unit = {
    damageQueue.clear()
  }
  // Measure whether a unit is doomed, counting only guaranteed sources of damage
  def doomed            : Boolean = doomFrameAbsolute < Forever()
  def doomedInFrames    : Int     = doomFrameAbsolute - With.frame
  def doomFrameAbsolute : Int     = {
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
  // Measure whether a unit is doomed, counting damage that has a chance to miss
  def likelyDoomed            : Boolean = likelyDoomFrameAbsolute < Forever()
  def likelyDoomedInFrames    : Int     = likelyDoomFrameAbsolute - With.frame
  def likelyDoomFrameAbsolute : Int     = {
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

  protected def _calculateInjury: Double = Maff.nanToZero((unitClass.maxTotalHealth - totalHealth).toDouble / unitClass.maxTotalHealth)
}
