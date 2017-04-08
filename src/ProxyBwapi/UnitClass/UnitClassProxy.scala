package ProxyBwapi.UnitClass

import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades
import bwapi.UnitType

import scala.collection.JavaConverters._

class UnitClassProxy(val baseType:UnitType) {
  lazy val abilities                = baseType.abilities.asScala.map(Techs.get)
  lazy val acceleration             = baseType.acceleration
  lazy val armor                    = baseType.armor
  lazy val armorUpgrade             = Upgrades.get(baseType.armorUpgrade)
  lazy val buildScore               = baseType.buildScore
  lazy val buildTime                = baseType.buildTime
  lazy val canAttack                = baseType.canAttack
  lazy val canBuildAddon            = baseType.canBuildAddon
  lazy val canMove                  = baseType.canMove
  lazy val canProduce               = baseType.canProduce
  lazy val cloakingTech             = Techs.get(baseType.cloakingTech)
  lazy val destroyScore             = baseType.destroyScore
  lazy val dimensionDown            = baseType.dimensionDown
  lazy val dimensionLeft            = baseType.dimensionLeft
  lazy val dimensionRight           = baseType.dimensionRight
  lazy val dimensionUp              = baseType.dimensionUp
  lazy val gasPrice                 = baseType.gasPrice
  lazy val haltDistance             = baseType.haltDistance
  lazy val permanentlyCloaked       = baseType.hasPermanentCloak
  lazy val height                   = baseType.height
  lazy val isAddon                  = baseType.isAddon
  lazy val isBeacon                 = baseType.isBeacon
  lazy val isBuilding               = baseType.isBuilding
  lazy val isBurrowable             = baseType.isBurrowable
  lazy val isCloakable              = baseType.isCloakable
  lazy val isCritter                = baseType.isCritter
  lazy val isDetector               = baseType.isDetector
  lazy val isFlagBeacon             = baseType.isFlagBeacon
  lazy val isFlyer                  = baseType.isFlyer
  lazy val isFlyingBuilding         = baseType.isFlyingBuilding
  lazy val isHero                   = baseType.isHero
  lazy val isInvincible             = baseType.isInvincible
  lazy val isMechanical             = baseType.isMechanical
  lazy val isMineralField           = baseType.isMineralField
  lazy val isNeutral                = baseType.isNeutral
  lazy val isOrganic                = baseType.isOrganic
  lazy val isPowerup                = baseType.isPowerup
  lazy val isRefinery               = baseType.isRefinery
  lazy val isResourceContainer      = baseType.isResourceContainer
  lazy val isResourceDepot          = baseType.isResourceDepot
  lazy val isRobotic                = baseType.isRobotic
  lazy val isSpecialBuilding        = baseType.isSpecialBuilding
  lazy val isSpell                  = baseType.isSpell
  lazy val isSpellcaster            = baseType.isSpellcaster
  lazy val isTwoUnitsInOneEgg       = baseType.isTwoUnitsInOneEgg
  lazy val isWorker                 = baseType.isWorker
  lazy val maxAirHits               = baseType.maxAirHits
  lazy val maxEnergy                = baseType.maxEnergy
  lazy val maxGroundHits            = baseType.maxGroundHits
  lazy val maxHitPoints             = baseType.maxHitPoints
  lazy val maxShields               = baseType.maxShields
  lazy val mineralPrice             = baseType.mineralPrice
  lazy val producesCreep            = baseType.producesCreep
  lazy val producesLarva            = baseType.producesLarva
  lazy val regeneratesHP            = baseType.regeneratesHP
  lazy val requiredTech             = Techs.get(baseType.requiredTech)
  lazy val requiredUnits            = baseType.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))
  lazy val requiresCreep            = baseType.requiresCreep
  lazy val requiresPsi              = baseType.requiresPsi
  lazy val researchesWhat           = baseType.researchesWhat.asScala.map(Techs.get)
  lazy val seekRange                = baseType.seekRange
  lazy val sightRange               = baseType.sightRange
  lazy val size                     = baseType.size
  lazy val spaceProvided            = baseType.spaceProvided
  lazy val spaceRequired            = baseType.spaceRequired
  lazy val supplyProvided           = baseType.supplyProvided
  lazy val supplyRequired           = baseType.supplyRequired
  lazy val tileHeight               = baseType.tileHeight
  lazy val tileSize                 = baseType.tileSize
  lazy val tileWidth                = baseType.tileWidth
  lazy val topSpeed                 = baseType.topSpeed
  lazy val turnRadius               = baseType.turnRadius
  lazy val upgrades                 = baseType.upgrades.asScala.map(Upgrades.get)
  lazy val upgradesWhat             = baseType.upgradesWhat.asScala.map(Upgrades.get)
  lazy val whatBuilds               = new Pair(UnitClasses.get(baseType.whatBuilds.first), baseType.whatBuilds.second)
  lazy val width                    = baseType.width
  lazy val race                     = baseType.getRace
  lazy val airWeapon                = baseType.airWeapon
  lazy val groundWeapon             = baseType.groundWeapon
  lazy val rawAirDamage             = baseType.airWeapon.damageAmount
  lazy val rawAirDamageBonus        = baseType.airWeapon.damageBonus
  lazy val rawAirDamageCooldown     = baseType.airWeapon.damageCooldown
  lazy val rawAirDamageFactor       = baseType.airWeapon.damageFactor
  lazy val rawAirDamageType         = baseType.airWeapon.damageType
  lazy val rawAirExplosionType      = baseType.airWeapon.explosionType
  lazy val rawAirRange              = baseType.airWeapon.maxRange
  lazy val rawGroundDamage          = baseType.groundWeapon.damageAmount
  lazy val rawGroundDamageBonus     = baseType.groundWeapon.damageBonus
  lazy val rawGroundDamageCooldown  = baseType.groundWeapon.damageCooldown
  lazy val rawGroundDamageFactor    = baseType.groundWeapon.damageFactor
  lazy val rawGroundDamageType      = baseType.groundWeapon.damageType
  lazy val rawGroundExplosionType   = baseType.groundWeapon.explosionType
  lazy val rawGroundMinRange        = baseType.groundWeapon.minRange
  lazy val rawGroundRange           = baseType.groundWeapon.maxRange
  lazy val asString                 = baseType.toString
}
