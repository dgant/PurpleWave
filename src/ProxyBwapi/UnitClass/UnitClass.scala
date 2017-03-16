package ProxyBwapi.UnitClass

import Geometry.TileRectangle
import Performance.Caching.CacheForever
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades
import Utilities.TypeEnrichment.EnrichUnitType._
import bwapi.{DamageType, TilePosition, UnitType}

import scala.collection.JavaConverters._

case class UnitClass(val base:UnitType) {
  def abilities             = new CacheForever(() => base.abilities.asScala.map(Techs.get)).get
  def acceleration          = new CacheForever(() => base.acceleration).get
  def armorUpgrade          = new CacheForever(() => Upgrades.get(base.armorUpgrade)).get
  def buildScore            = new CacheForever(() => base.buildScore).get
  def buildTime             = new CacheForever(() => base.buildTime).get
  def canAttack             = new CacheForever(() => base.canAttack).get
  def canBuildAddon         = new CacheForever(() => base.canBuildAddon).get
  def canMove               = new CacheForever(() => base.canMove).get
  def canProduce            = new CacheForever(() => base.canProduce).get
  def cloakingTech          = new CacheForever(() => Techs.get(base.cloakingTech)).get
  def destroyScore          = new CacheForever(() => base.destroyScore).get
  def dimensionDown         = new CacheForever(() => base.dimensionDown).get
  def dimensionLeft         = new CacheForever(() => base.dimensionLeft).get
  def dimensionRight        = new CacheForever(() => base.dimensionRight).get
  def dimensionUp           = new CacheForever(() => base.dimensionUp).get
  def gasPrice              = new CacheForever(() => base.gasPrice).get
  def haltDistance          = new CacheForever(() => base.haltDistance).get
  def permanentlyCloaked    = new CacheForever(() => base.hasPermanentCloak).get
  def height                = new CacheForever(() => base.height).get
  def isAddon               = new CacheForever(() => base.isAddon).get
  def isBeacon              = new CacheForever(() => base.isBeacon).get
  def isBuilding            = new CacheForever(() => base.isBuilding).get
  def isBurrowable          = new CacheForever(() => base.isBurrowable).get
  def isCloakable           = new CacheForever(() => base.isCloakable).get
  def isCritter             = new CacheForever(() => base.isCritter).get
  def isDetector            = new CacheForever(() => base.isDetector).get
  def isFlagBeacon          = new CacheForever(() => base.isFlagBeacon).get
  def isFlyer               = new CacheForever(() => base.isFlyer).get
  def isFlyingBuilding      = new CacheForever(() => base.isFlyingBuilding).get
  def isHero                = new CacheForever(() => base.isHero).get
  def isInvincible          = new CacheForever(() => base.isInvincible).get
  def isMechanical          = new CacheForever(() => base.isMechanical).get
  def isMineralField        = new CacheForever(() => base.isMineralField).get
  def isNeutral             = new CacheForever(() => base.isNeutral).get
  def isOrganic             = new CacheForever(() => base.isOrganic).get
  def isPowerup             = new CacheForever(() => base.isPowerup).get
  def isRefinery            = new CacheForever(() => base.isRefinery).get
  def isResourcesContainer  = new CacheForever(() => base.isResourceContainer).get
  def isResourceDepot       = new CacheForever(() => base.isResourceDepot).get
  def isRobotic             = new CacheForever(() => base.isRobotic).get
  def isSpecialBuilding     = new CacheForever(() => base.isSpecialBuilding).get
  def isSpell               = new CacheForever(() => base.isSpell).get
  def isSpellcaster         = new CacheForever(() => base.isSpellcaster).get
  def isTwoUnitsInOneEgg    = new CacheForever(() => base.isTwoUnitsInOneEgg).get
  def isWorker              = new CacheForever(() => base.isWorker).get
  def maxAirHits            = new CacheForever(() => base.maxAirHits).get
  def maxEnergy             = new CacheForever(() => base.maxEnergy).get
  def maxGroundHits         = new CacheForever(() => base.maxGroundHits).get
  def maxHitPoints          = new CacheForever(() => base.maxHitPoints).get
  def maxShields            = new CacheForever(() => base.maxShields).get
  def mineralPrice          = new CacheForever(() => base.mineralPrice).get
  def producesCreep         = new CacheForever(() => base.producesCreep).get
  def producesLarva         = new CacheForever(() => base.producesLarva).get
  def regeneratesHP         = new CacheForever(() => base.regeneratesHP).get
  def requiredTech          = new CacheForever(() => Techs.get(base.requiredTech)).get
  def requiredUnits         = new CacheForever(() => base.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))).get
  def requiresCreep         = new CacheForever(() => base.requiresCreep).get
  def requiresPsi           = new CacheForever(() => base.requiresPsi).get
  def researchesWhat        = new CacheForever(() => base.researchesWhat.asScala.map(Techs.get)).get
  def seekRange             = new CacheForever(() => base.seekRange).get
  def sightRange            = new CacheForever(() => base.sightRange).get
  def size                  = new CacheForever(() => base.size).get
  def spaceProvided         = new CacheForever(() => base.spaceProvided).get
  def spaceRequired         = new CacheForever(() => base.spaceRequired).get
  def supplyProvided        = new CacheForever(() => base.supplyProvided).get
  def supplyRequired        = new CacheForever(() => base.supplyRequired).get
  def tileHeight            = new CacheForever(() => base.tileHeight).get
  def tileSize              = new CacheForever(() => base.tileSize).get
  def tileWidth             = new CacheForever(() => base.tileWidth).get
  def topSpeed              = new CacheForever(() => base.topSpeed).get
  def turnRadius            = new CacheForever(() => base.turnRadius).get
  def upgrades              = new CacheForever(() => base.upgrades.asScala.map(Upgrades.get)).get
  def upgradesWhat          = new CacheForever(() => base.upgradesWhat.asScala.map(Upgrades.get)).get
  def whatBuilds            = new CacheForever(() => new Pair(UnitClasses.get(base.whatBuilds.first), base.whatBuilds.second)).get
  def width                 = new CacheForever(() => base.width).get
  def getRace               = new CacheForever(() => base.getRace).get
  def airWeapon             = new CacheForever(() => base.airWeapon).get
  def groundWeapon          = new CacheForever(() => base.groundWeapon).get
  
  //////////////////////////////////
  // Formerly from EnrichUnitType //
  //////////////////////////////////
  
  def groundDamage: Int = {
    def typeMultiplier =
      if (List(DamageType.Concussive, DamageType.Explosive).contains(base.groundWeapon.damageType())) {
        .75
      } else {
        1
      }
    def damage = typeMultiplier *
      base.maxGroundHits *
      base.groundWeapon.damageFactor *
      base.groundWeapon.damageAmount
    damage.toInt
  }
  def groundDps: Int = {
    def damagePerSecond = groundDamage * 24 / (2 + base.groundWeapon.damageCooldown)
    damagePerSecond.toInt
  }
  
  //Range is from unit edge, so we account for the diagonal width of the unit
  // 7/5 ~= sqrt(2)
  def range:Int = {
    if (base == UnitType.Terran_Bunker) { return UnitType.Terran_Marine.range }
    def range = List(base.groundWeapon.maxRange, base.airWeapon.maxRange).max
    range + base.width * 7 / 5
  }
  
  def totalCost: Int = { base.mineralPrice + base.gasPrice }
  def orderable:Boolean = ! Set(UnitType.Protoss_Interceptor, UnitType.Protoss_Scarab).contains(base)
  def isMinerals:Boolean = base.isMineralField
  def isGas:Boolean = List(UnitType.Resource_Vespene_Geyser, UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).contains(base)
  def isTownHall:Boolean = Set(
    UnitType.Terran_Command_Center,
    UnitType.Protoss_Nexus,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive
  ).contains(base)
  def area:TileRectangle =
    new TileRectangle(
      new TilePosition(0, 0),
      base.tileSize)
  def tiles:Iterable[TilePosition] = area.tiles
}
