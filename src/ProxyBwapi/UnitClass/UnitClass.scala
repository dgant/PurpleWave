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
  private val abilitiesCache             = new CacheForever(() => baseType.abilities.asScala.map(Techs.get))
  private val accelerationCache          = new CacheForever(() => baseType.acceleration)
  private val armorUpgradeCache          = new CacheForever(() => Upgrades.get(baseType.armorUpgrade))
  private val buildScoreCache            = new CacheForever(() => baseType.buildScore)
  private val buildTimeCache             = new CacheForever(() => baseType.buildTime)
  private val canAttackCache             = new CacheForever(() => baseType.canAttack)
  private val canBuildAddonCache         = new CacheForever(() => baseType.canBuildAddon)
  private val canMoveCache               = new CacheForever(() => baseType.canMove)
  private val canProduceCache            = new CacheForever(() => baseType.canProduce)
  private val cloakingTechCache          = new CacheForever(() => Techs.get(baseType.cloakingTech))
  private val destroyScoreCache          = new CacheForever(() => baseType.destroyScore)
  private val dimensionDownCache         = new CacheForever(() => baseType.dimensionDown)
  private val dimensionLeftCache         = new CacheForever(() => baseType.dimensionLeft)
  private val dimensionRightCache        = new CacheForever(() => baseType.dimensionRight)
  private val dimensionUpCache           = new CacheForever(() => baseType.dimensionUp)
  private val gasPriceCache              = new CacheForever(() => baseType.gasPrice)
  private val haltDistanceCache          = new CacheForever(() => baseType.haltDistance)
  private val permanentlyCloakedCache    = new CacheForever(() => baseType.hasPermanentCloak)
  private val heightCache                = new CacheForever(() => baseType.height)
  private val isAddonCache               = new CacheForever(() => baseType.isAddon)
  private val isBeaconCache              = new CacheForever(() => baseType.isBeacon)
  private val isBuildingCache            = new CacheForever(() => baseType.isBuilding)
  private val isBurrowableCache          = new CacheForever(() => baseType.isBurrowable)
  private val isCloakableCache           = new CacheForever(() => baseType.isCloakable)
  private val isCritterCache             = new CacheForever(() => baseType.isCritter)
  private val isDetectorCache            = new CacheForever(() => baseType.isDetector)
  private val isFlagBeaconCache          = new CacheForever(() => baseType.isFlagBeacon)
  private val isFlyerCache               = new CacheForever(() => baseType.isFlyer)
  private val isFlyingBuildingCache      = new CacheForever(() => baseType.isFlyingBuilding)
  private val isHeroCache                = new CacheForever(() => baseType.isHero)
  private val isInvincibleCache          = new CacheForever(() => baseType.isInvincible)
  private val isMechanicalCache          = new CacheForever(() => baseType.isMechanical)
  private val isMineralFieldCache        = new CacheForever(() => baseType.isMineralField)
  private val isNeutralCache             = new CacheForever(() => baseType.isNeutral)
  private val isOrganicCache             = new CacheForever(() => baseType.isOrganic)
  private val isPowerupCache             = new CacheForever(() => baseType.isPowerup)
  private val isRefineryCache            = new CacheForever(() => baseType.isRefinery)
  private val isResourceContainerCache   = new CacheForever(() => baseType.isResourceContainer)
  private val isResourceDepotCache       = new CacheForever(() => baseType.isResourceDepot)
  private val isRoboticCache             = new CacheForever(() => baseType.isRobotic)
  private val isSpecialBuildingCache     = new CacheForever(() => baseType.isSpecialBuilding)
  private val isSpellCache               = new CacheForever(() => baseType.isSpell)
  private val isSpellcasterCache         = new CacheForever(() => baseType.isSpellcaster)
  private val isTwoUnitsInOneEggCache    = new CacheForever(() => baseType.isTwoUnitsInOneEgg)
  private val isWorkerCache              = new CacheForever(() => baseType.isWorker)
  private val maxAirHitsCache            = new CacheForever(() => baseType.maxAirHits)
  private val maxEnergyCache             = new CacheForever(() => baseType.maxEnergy)
  private val maxGroundHitsCache         = new CacheForever(() => baseType.maxGroundHits)
  private val maxHitPointsCache          = new CacheForever(() => baseType.maxHitPoints)
  private val maxShieldsCache            = new CacheForever(() => baseType.maxShields)
  private val mineralPriceCache          = new CacheForever(() => baseType.mineralPrice)
  private val producesCreepCache         = new CacheForever(() => baseType.producesCreep)
  private val producesLarvaCache         = new CacheForever(() => baseType.producesLarva)
  private val regeneratesHPCache         = new CacheForever(() => baseType.regeneratesHP)
  private val requiredTechCache          = new CacheForever(() => Techs.get(baseType.requiredTech))
  private val requiredUnitsCache         = new CacheForever(() => baseType.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2)))
  private val requiresCreepCache         = new CacheForever(() => baseType.requiresCreep)
  private val requiresPsiCache           = new CacheForever(() => baseType.requiresPsi)
  private val researchesWhatCache        = new CacheForever(() => baseType.researchesWhat.asScala.map(Techs.get))
  private val seekRangeCache             = new CacheForever(() => baseType.seekRange)
  private val sightRangeCache            = new CacheForever(() => baseType.sightRange)
  private val sizeCache                  = new CacheForever(() => baseType.size)
  private val spaceProvidedCache         = new CacheForever(() => baseType.spaceProvided)
  private val spaceRequiredCache         = new CacheForever(() => baseType.spaceRequired)
  private val supplyProvidedCache        = new CacheForever(() => baseType.supplyProvided)
  private val supplyRequiredCache        = new CacheForever(() => baseType.supplyRequired)
  private val tileHeightCache            = new CacheForever(() => baseType.tileHeight)
  private val tileSizeCache              = new CacheForever(() => baseType.tileSize)
  private val tileWidthCache             = new CacheForever(() => baseType.tileWidth)
  private val topSpeedCache              = new CacheForever(() => baseType.topSpeed)
  private val turnRadiusCache            = new CacheForever(() => baseType.turnRadius)
  private val upgradesCache              = new CacheForever(() => baseType.upgrades.asScala.map(Upgrades.get))
  private val upgradesWhatCache          = new CacheForever(() => baseType.upgradesWhat.asScala.map(Upgrades.get))
  private val whatBuildsCache            = new CacheForever(() => new Pair(UnitClasses.get(baseType.whatBuilds.first), baseType.whatBuilds.second))
  private val widthCache                 = new CacheForever(() => baseType.width)
  private val getRaceCache               = new CacheForever(() => baseType.getRace)
  private val airWeaponCache             = new CacheForever(() => baseType.airWeapon)
  private val groundWeaponCache          = new CacheForever(() => baseType.groundWeapon)
  private val asStringCache              = new CacheForever(() => baseType.toString)
  
  def abilities             = abilitiesCache.get
  def acceleration          = accelerationCache.get
  def armorUpgrade          = armorUpgradeCache.get
  def buildScore            = buildScoreCache.get
  def buildTime             = buildTimeCache.get
  def canAttack             = canAttackCache.get
  def canBuildAddon         = canBuildAddonCache.get
  def canMove               = canMoveCache.get
  def canProduce            = canProduceCache.get
  def cloakingTech          = cloakingTechCache.get
  def destroyScore          = destroyScoreCache.get
  def dimensionDown         = dimensionDownCache.get
  def dimensionLeft         = dimensionLeftCache.get
  def dimensionRight        = dimensionRightCache.get
  def dimensionUp           = dimensionUpCache.get
  def gasPrice              = gasPriceCache.get
  def haltDistance          = haltDistanceCache.get
  def permanentlyCloaked    = permanentlyCloakedCache.get
  def height                = heightCache.get
  def isAddon               = isAddonCache.get
  def isBeacon              = isBeaconCache.get
  def isBuilding            = isBuildingCache.get
  def isBurrowaable         = isBurrowableCache.get
  def isCloakable           = isCloakableCache.get
  def isCritter             = isCritterCache.get
  def isDetector            = isDetectorCache.get
  def isFlagBeacon          = isFlagBeaconCache.get
  def isFlyer               = isFlyerCache.get
  def isFlyingBuilding      = isFlyingBuildingCache.get
  def isHero                = isHeroCache.get
  def isInvincible          = isInvincibleCache.get
  def isMechanical          = isMechanicalCache.get
  def isMineralField        = isMineralFieldCache.get
  def isNeutral             = isNeutralCache.get
  def isOrganic             = isOrganicCache.get
  def isPowerup             = isPowerupCache.get
  def isRefinery            = isRefineryCache.get
  def isResourceContainer   = isResourceContainerCache.get
  def isResourceDepot       = isResourceDepotCache.get
  def isRobotic             = isRoboticCache.get
  def isSpecialBuilding     = isSpecialBuildingCache.get
  def isSpell               = isSpellCache.get
  def isSpellcaster         = isSpellcasterCache.get
  def isTwoUnitsInOneEgg    = isTwoUnitsInOneEggCache.get
  def isWorker              = isWorkerCache.get
  def maxAirHits            = maxAirHitsCache.get
  def maxEnergy             = maxEnergyCache.get
  def maxHitPoints          = maxHitPointsCache.get
  def maxGroundHits         = maxGroundHitsCache.get
  def maxShields            = maxShieldsCache.get
  def mineralPrice          = mineralPriceCache.get
  def producesCreep         = producesCreepCache.get
  def producesLarva         = producesLarvaCache.get
  def regeneratesHP         = regeneratesHPCache.get
  def requiredTech          = requiredTechCache.get
  def requiredUnits         = requiredUnitsCache.get
  def requiresCreep         = requiresCreepCache.get
  def requiresPsi           = requiresPsiCache.get
  def researchesWhat        = researchesWhatCache.get
  def seekRange             = seekRangeCache.get
  def sightRange            = sightRangeCache.get
  def size                  = sizeCache.get
  def spaceProvided         = spaceProvidedCache.get
  def spaceRequired         = spaceRequiredCache.get
  def supplyProvided        = supplyProvidedCache.get
  def supplyRequired        = supplyRequiredCache.get
  def tileHeight            = tileHeightCache.get
  def tileSize              = tileSizeCache.get
  def tileWidth             = tileWidthCache.get
  def topSpeed              = topSpeedCache.get
  def turnRadius            = turnRadiusCache.get
  def upgrades              = upgradesCache.get
  def whatBuilds            = whatBuildsCache.get
  def width                 = widthCache.get
  def getRace               = getRaceCache.get
  def airWeapon             = airWeaponCache.get
  def groundWeapon          = groundWeaponCache.get
  def asString              = asStringCache.get
  
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
      tileSize)
  def tiles:Iterable[TilePosition] = area.tiles
  
  def behavior:Behavior = {
    if      (isWorker)                BehaviorWorker
    else if (isBuilding)              BehaviorBuilding
    else if (this == Protoss.Reaver)  BehaviorReaver
    else if (this == Protoss.Carrier) BehaviorCarrier
    else BehaviorDefault
  }
  
  override def toString:String =
    asString
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replace("Resource_", "")
      .replace("Special_", "")
      .replaceAll("_", " ")
}
