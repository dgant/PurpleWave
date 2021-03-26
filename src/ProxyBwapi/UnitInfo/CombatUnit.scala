package ProxyBwapi.UnitInfo

import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility

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

  @inline final def hitChanceAgainst(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Double = if (guaranteedToHit(enemy, from, to)) 1.0 else 0.47
  @inline final def guaranteedToHit(enemy: CombatUnit, from: Option[Pixel] = None, to: Option[Pixel] = None): Boolean = {
    val tileFrom  = from.getOrElse(pixel)       .tile
    val tileTo    =   to.getOrElse(enemy.pixel) .tile
    flying || enemy.flying || unitClass.unaffectedByDarkSwarm || tileFrom.altitude >= tileTo.altitude
  }

  @inline final def damageTypeAgainst (enemy: CombatUnit)  : Damage.Type  = if (enemy.flying) unitClass.airDamageType    else unitClass.groundDamageType
  @inline final def attacksAgainst    (enemy: CombatUnit)  : Int          = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  @inline final def dpfOnNextHitAgainst(enemy: UnitInfo): Double =
    if (unitClass.suicides) damageOnNextHitAgainst(enemy) else {
      val cooldownVs = cooldownMaxAgainst(enemy)
      if (cooldownVs == 0) 0.0 else damageOnNextHitAgainst(enemy).toDouble / cooldownVs
    }

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
}
