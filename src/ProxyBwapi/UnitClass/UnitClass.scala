package ProxyBwapi.UnitClass

import Geometry.TileRectangle
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades
import bwapi.{DamageType, TilePosition, UnitType}

import Utilities.TypeEnrichment.EnrichUnitType._

import scala.collection.JavaConverters._

case class UnitClass(val base:UnitType) {
  val abilities             = base.abilities.asScala.map(Techs.get)
  val acceleration          = base.acceleration
  val armorUpgrade          = Upgrades.get(base.armorUpgrade)
  val buildScore            = base.buildScore
  val buildTime             = base.buildTime
  val canAttack             = base.canAttack
  val canBuildAddon         = base.canBuildAddon
  val canMove               = base.canMove
  val canProduce            = base.canProduce
  val cloakingTech          = Techs.get(base.cloakingTech)
  val destroyScore          = base.destroyScore
  val dimensionDown         = base.dimensionDown
  val dimensionLeft         = base.dimensionLeft
  val dimensionRight        = base.dimensionRight
  val dimensionUp           = base.dimensionUp
  val gasPrice              = base.gasPrice
  val haltDistance          = base.haltDistance
  val permanentlyCloaked    = base.hasPermanentCloak
  val height                = base.height
  val isAddon               = base.isAddon
  val isBeacon              = base.isBeacon
  val isBuilding            = base.isBuilding
  val isBurrowable          = base.isBurrowable
  val isCloakable           = base.isCloakable
  val isCritter             = base.isCritter
  val isDetector            = base.isDetector
  val isFlagBeacon          = base.isFlagBeacon
  val isFlyer               = base.isFlyer
  val isFlyingBuilding      = base.isFlyingBuilding
  val isHero                = base.isHero
  val isInvincible          = base.isInvincible
  val isMechanical          = base.isMechanical
  val isMineralField        = base.isMineralField
  val isNeutral             = base.isNeutral
  val isOrganic             = base.isOrganic
  val isPowerup             = base.isPowerup
  val isRefinery            = base.isRefinery
  val isResourcesContainer  = base.isResourceContainer
  val isResourceDepot       = base.isResourceDepot
  val isRobotic             = base.isRobotic
  val isSpecialBuilding     = base.isSpecialBuilding
  val isSpell               = base.isSpell
  val isSpellcaster         = base.isSpellcaster
  val isTwoUnitsInOneEgg    = base.isTwoUnitsInOneEgg
  val isWorker              = base.isWorker
  val maxAirHits            = base.maxAirHits
  val maxEnergy             = base.maxEnergy
  val maxGroundHits         = base.maxGroundHits
  val maxHitPoints          = base.maxHitPoints
  val maxShields            = base.maxShields
  val mineralPrice          = base.mineralPrice
  val producesCreep         = base.producesCreep
  val producesLarva         = base.producesLarva
  val regeneratesHP         = base.regeneratesHP
  val requiredTech          = Techs.get(base.requiredTech)
  val requiredUnits         = base.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))
  val requiresCreep         = base.requiresCreep
  val requiresPsi           = base.requiresPsi
  val researchesWhat        = base.researchesWhat.asScala.map(Techs.get)
  val seekRange             = base.seekRange
  val sightRange            = base.sightRange
  val size                  = base.size
  val spaceProvided         = base.spaceProvided
  val spaceRequired         = base.spaceRequired
  val supplyProvided        = base.supplyProvided
  val supplyRequired        = base.supplyRequired
  val tileHeight            = base.tileHeight
  val tileSize              = base.tileSize
  val tileWidth             = base.tileWidth
  val topSpeed              = base.topSpeed
  val turnRadius            = base.turnRadius
  val upgrades              = base.upgrades.asScala.map(Upgrades.get)
  val upgradesWhat          = base.upgradesWhat.asScala.map(Upgrades.get)
  val whatBuilds            = new Pair(UnitClasses.get(base.whatBuilds.first), base.whatBuilds.second)
  val width = base.width
  
  //TODO: Replace
  val getRace = base.getRace
  val airWeapon = base.airWeapon
  val groundWeapon = base.groundWeapon
  
  //////////////////////////////////
  // Formerly from EnrichUnitType //
  //////////////////////////////////
  
  def groundDamage: Int = {
    val typeMultiplier =
      if (List(DamageType.Concussive, DamageType.Explosive).contains(base.groundWeapon.damageType())) {
        .75
      } else {
        1
      }
    val damage = typeMultiplier *
      base.maxGroundHits *
      base.groundWeapon.damageFactor *
      base.groundWeapon.damageAmount
    damage.toInt
  }
  def groundDps: Int = {
    val damagePerSecond = groundDamage * 24 / (2 + base.groundWeapon.damageCooldown)
    damagePerSecond.toInt
  }
  
  //Range is from unit edge, so we account for the diagonal width of the unit
  // 7/5 ~= sqrt(2)
  def range:Int = {
    if (base == UnitType.Terran_Bunker) { return UnitType.Terran_Marine.range }
    val range = List(base.groundWeapon.maxRange, base.airWeapon.maxRange).max
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
