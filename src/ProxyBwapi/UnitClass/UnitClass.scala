package ProxyBwapi.UnitClass

import Geometry.TileRectangle
import Micro.Behaviors.Protoss.{BehaviorCarrier, BehaviorReaver}
import Micro.Behaviors.{Behavior, BehaviorBuilding, BehaviorDefault, BehaviorWorker}
import Performance.Caching.CacheForever
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades
import bwapi.{DamageType, TilePosition, UnitType}

import scala.collection.JavaConverters._

case class UnitClass(val baseType:UnitType) {
  def abilities             = new CacheForever(() => baseType.abilities.asScala.map(Techs.get)).get
  def acceleration          = new CacheForever(() => baseType.acceleration).get
  def armorUpgrade          = new CacheForever(() => Upgrades.get(baseType.armorUpgrade)).get
  def buildScore            = new CacheForever(() => baseType.buildScore).get
  def buildTime             = new CacheForever(() => baseType.buildTime).get
  def canAttack             = new CacheForever(() => baseType.canAttack).get
  def canBuildAddon         = new CacheForever(() => baseType.canBuildAddon).get
  def canMove               = new CacheForever(() => baseType.canMove).get
  def canProduce            = new CacheForever(() => baseType.canProduce).get
  def cloakingTech          = new CacheForever(() => Techs.get(baseType.cloakingTech)).get
  def destroyScore          = new CacheForever(() => baseType.destroyScore).get
  def dimensionDown         = new CacheForever(() => baseType.dimensionDown).get
  def dimensionLeft         = new CacheForever(() => baseType.dimensionLeft).get
  def dimensionRight        = new CacheForever(() => baseType.dimensionRight).get
  def dimensionUp           = new CacheForever(() => baseType.dimensionUp).get
  def gasPrice              = new CacheForever(() => baseType.gasPrice).get
  def haltDistance          = new CacheForever(() => baseType.haltDistance).get
  def permanentlyCloaked    = new CacheForever(() => baseType.hasPermanentCloak).get
  def height                = new CacheForever(() => baseType.height).get
  def isAddon               = new CacheForever(() => baseType.isAddon).get
  def isBeacon              = new CacheForever(() => baseType.isBeacon).get
  def isBuilding            = new CacheForever(() => baseType.isBuilding).get
  def isBurrowable          = new CacheForever(() => baseType.isBurrowable).get
  def isCloakable           = new CacheForever(() => baseType.isCloakable).get
  def isCritter             = new CacheForever(() => baseType.isCritter).get
  def isDetector            = new CacheForever(() => baseType.isDetector).get
  def isFlagBeacon          = new CacheForever(() => baseType.isFlagBeacon).get
  def isFlyer               = new CacheForever(() => baseType.isFlyer).get
  def isFlyingBuilding      = new CacheForever(() => baseType.isFlyingBuilding).get
  def isHero                = new CacheForever(() => baseType.isHero).get
  def isInvincible          = new CacheForever(() => baseType.isInvincible).get
  def isMechanical          = new CacheForever(() => baseType.isMechanical).get
  def isMineralField        = new CacheForever(() => baseType.isMineralField).get
  def isNeutral             = new CacheForever(() => baseType.isNeutral).get
  def isOrganic             = new CacheForever(() => baseType.isOrganic).get
  def isPowerup             = new CacheForever(() => baseType.isPowerup).get
  def isRefinery            = new CacheForever(() => baseType.isRefinery).get
  def isResourcesContainer  = new CacheForever(() => baseType.isResourceContainer).get
  def isResourceDepot       = new CacheForever(() => baseType.isResourceDepot).get
  def isRobotic             = new CacheForever(() => baseType.isRobotic).get
  def isSpecialBuilding     = new CacheForever(() => baseType.isSpecialBuilding).get
  def isSpell               = new CacheForever(() => baseType.isSpell).get
  def isSpellcaster         = new CacheForever(() => baseType.isSpellcaster).get
  def isTwoUnitsInOneEgg    = new CacheForever(() => baseType.isTwoUnitsInOneEgg).get
  def isWorker              = new CacheForever(() => baseType.isWorker).get
  def maxAirHits            = new CacheForever(() => baseType.maxAirHits).get
  def maxEnergy             = new CacheForever(() => baseType.maxEnergy).get
  def maxGroundHits         = new CacheForever(() => baseType.maxGroundHits).get
  def maxHitPoints          = new CacheForever(() => baseType.maxHitPoints).get
  def maxShields            = new CacheForever(() => baseType.maxShields).get
  def mineralPrice          = new CacheForever(() => baseType.mineralPrice).get
  def producesCreep         = new CacheForever(() => baseType.producesCreep).get
  def producesLarva         = new CacheForever(() => baseType.producesLarva).get
  def regeneratesHP         = new CacheForever(() => baseType.regeneratesHP).get
  def requiredTech          = new CacheForever(() => Techs.get(baseType.requiredTech)).get
  def requiredUnits         = new CacheForever(() => baseType.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))).get
  def requiresCreep         = new CacheForever(() => baseType.requiresCreep).get
  def requiresPsi           = new CacheForever(() => baseType.requiresPsi).get
  def researchesWhat        = new CacheForever(() => baseType.researchesWhat.asScala.map(Techs.get)).get
  def seekRange             = new CacheForever(() => baseType.seekRange).get
  def sightRange            = new CacheForever(() => baseType.sightRange).get
  def size                  = new CacheForever(() => baseType.size).get
  def spaceProvided         = new CacheForever(() => baseType.spaceProvided).get
  def spaceRequired         = new CacheForever(() => baseType.spaceRequired).get
  def supplyProvided        = new CacheForever(() => baseType.supplyProvided).get
  def supplyRequired        = new CacheForever(() => baseType.supplyRequired).get
  def tileHeight            = new CacheForever(() => baseType.tileHeight).get
  def tileSize              = new CacheForever(() => baseType.tileSize).get
  def tileWidth             = new CacheForever(() => baseType.tileWidth).get
  def topSpeed              = new CacheForever(() => baseType.topSpeed).get
  def turnRadius            = new CacheForever(() => baseType.turnRadius).get
  def upgrades              = new CacheForever(() => baseType.upgrades.asScala.map(Upgrades.get)).get
  def upgradesWhat          = new CacheForever(() => baseType.upgradesWhat.asScala.map(Upgrades.get)).get
  def whatBuilds            = new CacheForever(() => new Pair(UnitClasses.get(baseType.whatBuilds.first), baseType.whatBuilds.second)).get
  def width                 = new CacheForever(() => baseType.width).get
  def getRace               = new CacheForever(() => baseType.getRace).get
  def airWeapon             = new CacheForever(() => baseType.airWeapon).get
  def groundWeapon          = new CacheForever(() => baseType.groundWeapon).get
  
  //////////////////////////////////
  // Formerly from EnrichUnitType //
  //////////////////////////////////
  
  def groundDamage: Int = {
    def typeMultiplier =
      if (List(DamageType.Concussive, DamageType.Explosive).contains(baseType.groundWeapon.damageType())) {
        .75
      } else {
        1
      }
    def damage = typeMultiplier *
      baseType.maxGroundHits *
      baseType.groundWeapon.damageFactor *
      baseType.groundWeapon.damageAmount
    damage.toInt
  }
  def groundDps: Int = {
    def damagePerSecond = groundDamage * 24 / (2 + baseType.groundWeapon.damageCooldown)
    damagePerSecond.toInt
  }
  
  //Range is from unit edge, so we account for the diagonal width of the unit
  // 7/5 ~= sqrt(2)
  def range:Int = {
    if (this == Terran.Bunker) return Terran.Marine.range
    def range = List(baseType.groundWeapon.maxRange, baseType.airWeapon.maxRange).max
    range + baseType.width * 7 / 5
  }
  
  def totalCost: Int = { baseType.mineralPrice + baseType.gasPrice }
  def orderable:Boolean = ! Set(UnitType.Protoss_Interceptor, UnitType.Protoss_Scarab).contains(baseType)
  def isMinerals:Boolean = baseType.isMineralField
  def isGas:Boolean = List(UnitType.Resource_Vespene_Geyser, UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).contains(baseType)
  def isTownHall:Boolean = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  ).contains(baseType)
  def area:TileRectangle =
    new TileRectangle(
      new TilePosition(0, 0),
      baseType.tileSize)
  def tiles:Iterable[TilePosition] = area.tiles
  
  def behavior:Behavior = {
    if      (isWorker)                BehaviorWorker
    else if (isBuilding)              BehaviorBuilding
    else if (this == Protoss.Reaver)  BehaviorReaver
    else if (this == Protoss.Carrier) BehaviorCarrier
    else BehaviorDefault
  }
  
  override def toString:String =
    baseType.toString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replace("Resource_", "")
      .replace("Special_", "")
      .replaceAll("_", " ")
}
