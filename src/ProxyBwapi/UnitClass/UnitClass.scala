package ProxyBwapi.UnitClass

import Geometry.TileRectangle
import Micro.Behaviors.Protoss.{BehaviorCarrier, BehaviorCorsair, BehaviorDarkTemplar, BehaviorReaver}
import Micro.Behaviors._
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}
import bwapi.{DamageType, TilePosition, UnitType}

case class UnitClass(base:UnitType) extends UnitClassProxy(base) {
  
  def radialHypotenuse = Math.sqrt(width.toDouble * width.toDouble + height.toDouble * height.toDouble)/2.0
  
  //Don't use this for Dark Swarm -- just use a fixed set of units
  def isMelee: Boolean = groundRange <= 32 && ! isWorker
  
  //TODO: Explosive is 50/75/100
  //But Concussive is 25/50/100, not 50/75/100 !!!
  private val concussiveOrExplosive = List(DamageType.Concussive, DamageType.Explosive)
  def effectiveAirDamage:Double = {
    if (this == Protoss.Carrier)      return Protoss.Interceptor.effectiveAirDamage * 8
    if (this == Protoss.Interceptor)  return 0
    if (this == Terran.Bunker)        return Terran.Marine.effectiveAirDamage * 4
    val typeMultiplier = if (concussiveOrExplosive.contains(rawAirDamageType)) 0.75 else 1.0
    typeMultiplier *
      rawMaxAirHits *
      rawAirDamageFactor *
      rawAirDamage
  }
  def effectiveGroundDamage:Double = {
    if (this == Protoss.Carrier)      return Protoss.Interceptor.effectiveGroundDamage * 8
    if (this == Protoss.Interceptor)  return 0
    if (this == Protoss.Reaver)       return Protoss.Scarab.effectiveGroundDamage
    if (this == Terran.Bunker)        return Terran.Marine.effectiveGroundDamage * 4
    
    val typeMultiplier = if (concussiveOrExplosive.contains(rawGroundDamageType)) 0.75 else 1.0
    typeMultiplier *
      maxGroundHits *
      rawGroundDamageFactor *
      rawGroundDamage
  }
  
  def airDamageCooldown:Int = rawAirDamageCooldown
  def groundDamageCooldown:Int = {
    //Necessary according to Skynet: https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/Unit.cpp#L1092
    if (this == Protoss.Reaver) return 60
    return rawGroundDamageCooldown
  }
  
  //The extra 2+ is to account for the 1-3 frame random variation in cooldown
  def airDps    : Double = effectiveAirDamage    * 24 / (2 + airDamageCooldown).toDouble
  def groundDps : Double = effectiveGroundDamage * 24 / (2 + groundDamageCooldown).toDouble
  
  def attacksGround : Boolean = effectiveGroundDamage > 0
  def attacksAir    : Boolean = effectiveAirDamage    > 0
  
  def helpsInCombat:Boolean = canAttack || isSpellcaster || Set(Terran.Bunker, Terran.Medic).contains(this)
  
  //Range is from unit edge, so we account for the diagonal width of the unit
  // 7/5 ~= sqrt(2)
  def groundRange:Int = {
    if (this == Terran.Bunker) return Terran.Marine.groundRange + 32
    rawGroundRange
  }
  def airRange:Int = {
    if (this == Terran.Bunker) return Terran.Marine.airRange + 32
    rawAirRange
  }
  def maxAirGroundRange:Int = {
    Math.max(groundRange, airRange)
  }
  
  def isResource:Boolean = isMinerals || isGas
  def maxTotalHealth:Int = maxHitPoints + maxShields
  def totalCost: Int = mineralPrice + gasPrice
  def orderable:Boolean = ! isSpell && ! Set(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine).contains(this)
  def isMinerals:Boolean = isMineralField
  def isGas:Boolean = List(Neutral.Geyser, Terran.Refinery, Protoss.Assimilator, Zerg.Extractor).contains(this)
  def isTownHall:Boolean = Set(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery, Zerg.Lair, Zerg.Hive).contains(this)
  def tileArea:TileRectangle = new TileRectangle(new TilePosition(0, 0), tileSize)
  
  def behavior:Behavior = {
    if      (isWorker)                    BehaviorWorker
    else if (isBuilding)                  BehaviorBuilding
    else if (this == Protoss.Carrier)     BehaviorCarrier
    else if (this == Protoss.Corsair)     BehaviorCorsair
    else if (this == Protoss.DarkTemplar) BehaviorDarkTemplar
    else if (this == Protoss.Dragoon)     BehaviorDragoon
    else if (this == Protoss.Reaver)      BehaviorReaver
    else if (this == Protoss.Scout)       BehaviorCorsair
    else                                  BehaviorDefault
  }
  
  override def toString:String =
    asString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replace("Resource_", "")
      .replace("Critter_", "")
      .replace("Special_", "")
      .replaceAll("_", " ")
}
