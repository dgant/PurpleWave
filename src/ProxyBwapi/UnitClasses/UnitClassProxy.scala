package ProxyBwapi.UnitClasses

import Mathematics.Points.Tile
import ProxyBwapi.Engine.{Damage, Size}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi.UnitType

import scala.collection.JavaConverters._

abstract class UnitClassProxy(val bwapiType: UnitType) {
  lazy val id                       = bwapiType.id
  lazy val abilities                = bwapiType.abilities.asScala.map(Techs.get)
  lazy val acceleration             = bwapiType.acceleration
  lazy val armor                    = bwapiType.armor
  lazy val buildScore               = bwapiType.buildScore
  lazy val buildFrames              = bwapiType.buildTime
  lazy val rawCanAttack             = bwapiType.canAttack
  lazy val canBuildAddon            = bwapiType.canBuildAddon
  lazy val canMove                  = bwapiType.canMove
  lazy val canProduce               = bwapiType.canProduce
  lazy val cloakingTech             = Techs.get(bwapiType.cloakingTech)
  lazy val destroyScore             = bwapiType.destroyScore
  lazy val dimensionDown            = bwapiType.dimensionDown
  lazy val dimensionLeft            = bwapiType.dimensionLeft
  lazy val dimensionRight           = bwapiType.dimensionRight
  lazy val dimensionUp              = bwapiType.dimensionUp
  lazy val gasPrice                 = bwapiType.gasPrice
  lazy val haltPixels               = if (this == Protoss.DarkTemplar) 0 else bwapiType.haltDistance / 256
  lazy val permanentlyCloaked       = bwapiType.hasPermanentCloak
  lazy val height                   = bwapiType.height
  lazy val isAddon                  = bwapiType.isAddon
  lazy val isBeacon                 = bwapiType.isBeacon
  lazy val isBuilding               = bwapiType.isBuilding
  lazy val isBurrowable             = bwapiType.isBurrowable
  lazy val isCloakable              = bwapiType.isCloakable
  lazy val isCritter                = bwapiType.isCritter
  lazy val isDetector               = bwapiType.isDetector
  lazy val isFlagBeacon             = bwapiType.isFlagBeacon
  lazy val isFlyer                  = bwapiType.isFlyer
  lazy val isFlyingBuilding         = bwapiType.isFlyingBuilding
  lazy val isHero                   = bwapiType.isHero
  lazy val isInvincible             = bwapiType.isInvincible
  lazy val isMechanical             = bwapiType.isMechanical
  lazy val isMineralField           = bwapiType.isMineralField
  lazy val isNeutral                = bwapiType.isNeutral
  lazy val isOrganic                = bwapiType.isOrganic
  lazy val isPowerup                = bwapiType.isPowerup
  lazy val isRefinery               = bwapiType.isRefinery
  lazy val isResourceContainer      = bwapiType.isResourceContainer
  lazy val isResourceDepot          = bwapiType.isResourceDepot
  lazy val isRobotic                = bwapiType.isRobotic
  lazy val isSpecialBuilding        = bwapiType.isSpecialBuilding
  lazy val isSpell                  = bwapiType.isSpell
  lazy val isSpellcaster            = bwapiType.isSpellcaster
  lazy val isTwoUnitsInOneEgg       = bwapiType.isTwoUnitsInOneEgg
  lazy val isWorker                 = bwapiType.isWorker
  lazy val maxAirHitsRaw            = bwapiType.maxAirHits
  lazy val maxEnergy                = bwapiType.maxEnergy
  lazy val maxGroundHitsRaw         = bwapiType.maxGroundHits
  lazy val maxHitPoints             = bwapiType.maxHitPoints
  lazy val maxShields               = bwapiType.maxShields
  lazy val mineralPrice             = bwapiType.mineralPrice
  lazy val producesCreep            = bwapiType.producesCreep
  lazy val producesLarva            = bwapiType.producesLarva
  lazy val regeneratesHP            = bwapiType.regeneratesHP
  lazy val requiredTechRaw          = Techs.get(bwapiType.requiredTech)
  lazy val requiredUnits            = bwapiType.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))
  lazy val requiresCreep            = bwapiType.requiresCreep
  lazy val requiresPsi              = bwapiType.requiresPsi
  lazy val techsWhat                = bwapiType.researchesWhat.asScala.map(Techs.get)
  lazy val seekRange                = bwapiType.seekRange
  lazy val sightRangePixels         = bwapiType.sightRange
  lazy val spaceProvided            = bwapiType.spaceProvided
  lazy val spaceRequired            = bwapiType.spaceRequired
  lazy val supplyProvided           = bwapiType.supplyProvided
  lazy val supplyRequired           = bwapiType.supplyRequired
  lazy val tileHeight               = bwapiType.tileHeight
  lazy val tileSize                 = new Tile(bwapiType.tileSize)
  lazy val tileWidth                = bwapiType.tileWidth
  lazy val topSpeed                 = bwapiType.topSpeed
  lazy val turnRadius               = bwapiType.turnRadius
  lazy val upgrades                 = bwapiType.upgrades.asScala.map(Upgrades.get)
  lazy val upgradesWhat             = bwapiType.upgradesWhat.asScala.map(Upgrades.get)
  lazy val whatBuilds               = new Pair(UnitClasses.get(bwapiType.whatBuilds.getKey), bwapiType.whatBuilds.getValue)
  lazy val width                    = bwapiType.width
  lazy val race                     = bwapiType.getRace
  lazy val sizeRaw                  = size
  lazy val airWeaponRaw             = bwapiType.airWeapon
  lazy val groundWeaponRaw          = bwapiType.groundWeapon
  lazy val airDamageRaw             = bwapiType.airWeapon.damageAmount
  lazy val airDamageBonusRaw        = bwapiType.airWeapon.damageBonus
  lazy val airDamageCooldownRaw     = bwapiType.airWeapon.damageCooldown
  lazy val airDamageFactorRaw       = bwapiType.airWeapon.damageFactor
  lazy val airDamageTypeRaw         = bwapiType.airWeapon.damageType
  lazy val airExplosionTypeRaw      = bwapiType.airWeapon.explosionType
  lazy val airRangeRaw              = bwapiType.airWeapon.maxRange
  lazy val airSplashRadius50        = bwapiType.airWeapon.innerSplashRadius
  lazy val airSplashRadius25        = bwapiType.airWeapon.outerSplashRadius
  lazy val groundDamageRaw          = bwapiType.groundWeapon.damageAmount
  lazy val groundDamageBonusRaw     = bwapiType.groundWeapon.damageBonus
  lazy val groundDamageCooldownRaw  = bwapiType.groundWeapon.damageCooldown
  lazy val groundDamageFactorRaw    = bwapiType.groundWeapon.damageFactor
  lazy val groundDamageTypeRaw      = bwapiType.groundWeapon.damageType
  lazy val groundExplosionTypeRaw   = bwapiType.groundWeapon.explosionType
  lazy val groundSplashRadius50     = bwapiType.groundWeapon.innerSplashRadius
  lazy val groundSplashRadius25     = bwapiType.groundWeapon.outerSplashRadius
  lazy val groundMinRangeRaw        = bwapiType.groundWeapon.minRange
  lazy val groundRangeRaw           = bwapiType.groundWeapon.maxRange
  lazy val asString                 = bwapiType.toString

  // .size is broken in BWMirror. This is a manual replacement.
  // Data via http://classic.battle.net/scc/GS/damage.shtml
  lazy val size: Size.Type = {
    if (Vector(
      UnitType.Terran_SCV,
      UnitType.Terran_Marine,
      UnitType.Terran_Firebat,
      UnitType.Terran_Medic,
      UnitType.Protoss_Probe,
      UnitType.Protoss_Zealot,
      UnitType.Protoss_High_Templar,
      UnitType.Protoss_Dark_Templar,
      UnitType.Protoss_Observer,
      UnitType.Protoss_Interceptor,
      UnitType.Zerg_Larva,
      UnitType.Zerg_Drone,
      UnitType.Zerg_Zergling,
      UnitType.Zerg_Infested_Terran,
      UnitType.Zerg_Broodling,
      UnitType.Zerg_Scourge,
      UnitType.Zerg_Mutalisk
    ).contains(bwapiType))
      Size.Small
    else if (Vector(
      UnitType.Terran_Vulture,
      UnitType.Protoss_Corsair,
      UnitType.Zerg_Hydralisk,
      UnitType.Zerg_Defiler,
      UnitType.Zerg_Queen,
      UnitType.Zerg_Lurker
    ).contains(bwapiType))
      Size.Medium
    else
      Size.Large
  }

  lazy val groundDamageType: Damage.Type = {
    if (Vector(
      UnitType.Terran_Vulture,
      UnitType.Terran_Ghost,
      UnitType.Terran_Firebat
    ).contains(bwapiType))
      Damage.Concussive
    else if (Vector(
      UnitType.Terran_Vulture_Spider_Mine,
      UnitType.Terran_Siege_Tank_Siege_Mode,
      UnitType.Terran_Siege_Tank_Tank_Mode,
      UnitType.Zerg_Hydralisk,
      UnitType.Zerg_Infested_Terran,
      UnitType.Zerg_Sunken_Colony,
      UnitType.Protoss_Dragoon,
      UnitType.Protoss_Arbiter
    ).contains(bwapiType))
      Damage.Explosive
    else
      Damage.Normal
  }

  lazy val airDamageType: Damage.Type = {
    if (Vector(
      UnitType.Terran_Ghost
    ).contains(bwapiType))
      Damage.Concussive
    else if (Vector(
      UnitType.Terran_Goliath,
      UnitType.Terran_Wraith,
      UnitType.Terran_Valkyrie,
      UnitType.Terran_Missile_Turret,
      UnitType.Zerg_Hydralisk,
      UnitType.Zerg_Devourer,
      UnitType.Protoss_Dragoon,
      UnitType.Protoss_Arbiter,
      UnitType.Protoss_Scout,
      UnitType.Protoss_Corsair
    ).contains(bwapiType))
      Damage.Explosive
    else
      Damage.Normal
  }
  
  
  lazy val armorUpgrade: Option[Upgrade] = {
    if (Vector(
      Terran.Marine,
      Terran.Firebat,
      Terran.Medic,
      Terran.Ghost
    ).contains(this))
      Some(Terran.BioArmor)
    else if (Vector(
      Terran.Vulture,
      Terran.Goliath,
      Terran.SiegeTankSieged,
      Terran.SiegeTankUnsieged
    ).contains(this))
      Some(Terran.MechArmor)
    else if (Vector(
      Terran.Wraith,
      Terran.Valkyrie,
      Terran.Battlecruiser,
      Terran.ScienceVessel
    ).contains(this))
      Some(Terran.AirArmor)
    else if (Vector(
      Protoss.Zealot,
      Protoss.Dragoon,
      Protoss.DarkTemplar,
      Protoss.Archon,
      Protoss.HighTemplar,
      Protoss.DarkArchon,
      Protoss.Reaver
    ).contains(this))
      Some(Protoss.GroundArmor)
    else if(Vector(
      Protoss.Scout,
      Protoss.Corsair,
      Protoss.Carrier,
      Protoss.Interceptor,
      Protoss.Arbiter
    ).contains(this))
      Some(Protoss.AirArmor)
    else if(Vector(
      Zerg.Zergling,
      Zerg.Ultralisk,
      Zerg.Hydralisk,
      Zerg.Lurker,
      Zerg.Defiler
    ).contains(this))
      Some(Zerg.GroundArmor)
    else if(Vector(
      Zerg.Overlord,
      Zerg.Mutalisk,
      Zerg.Guardian,
      Zerg.Devourer,
      Zerg.Queen
    ).contains(this))
      Some(Zerg.AirArmor)
    else
      None
  }
  
  lazy val damageUpgradeType: Option[Upgrade] = {
    if (Vector(
      Terran.Marine,
      Terran.Firebat,
      Terran.Ghost
    ).contains(this))
      Some(Terran.BioDamage)
    else if (Vector(
      Terran.Vulture,
      Terran.Goliath,
      Terran.SiegeTankSieged,
      Terran.SiegeTankUnsieged
    ).contains(this))
      Some(Terran.MechDamage)
    else if (Vector(
      Terran.Wraith,
      Terran.Valkyrie,
      Terran.Battlecruiser
    ).contains(this))
      Some(Terran.AirDamage)
    else if (Vector(
      Protoss.Zealot,
      Protoss.Dragoon,
      Protoss.DarkTemplar,
      Protoss.Archon
    ).contains(this))
      Some(Protoss.GroundDamage)
    else if(Vector(
      Protoss.Scout,
      Protoss.Corsair,
      Protoss.Carrier,
      Protoss.Interceptor,
      Protoss.Arbiter
    ).contains(this))
      Some(Protoss.AirArmor)
    else if(Vector(
      Protoss.Reaver,
      Protoss.Scarab
    ).contains(this))
      Some(Protoss.ScarabDamage)
    else if(Vector(
      Zerg.Zergling,
      Zerg.Ultralisk
    ).contains(this))
      Some(Zerg.GroundMeleeDamage)
    else if(Vector(
      Zerg.Hydralisk,
      Zerg.Lurker
    ).contains(this))
      Some(Zerg.GroundRangeDamage)
    else if(Vector(
      Zerg.Mutalisk,
      Zerg.Guardian,
      Zerg.Devourer
    ).contains(this))
      Some(Zerg.AirDamage)
    else
      None
  }
}
