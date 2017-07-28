package ProxyBwapi.UnitClass

import Mathematics.Points.Tile
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi.{DamageType, UnitSizeType, UnitType}

import scala.collection.JavaConverters._

class UnitClassProxy(val baseType:UnitType) {
  lazy val abilities                = baseType.abilities.asScala.map(Techs.get)
  lazy val acceleration             = baseType.acceleration
  lazy val armor                    = baseType.armor
  lazy val buildScore               = baseType.buildScore
  lazy val buildFrames              = baseType.buildTime
  lazy val rawCanAttack             = baseType.canAttack
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
  lazy val maxAirHitsRaw            = baseType.maxAirHits
  lazy val maxEnergy                = baseType.maxEnergy
  lazy val maxGroundHitsRaw         = baseType.maxGroundHits
  lazy val maxHitPoints             = baseType.maxHitPoints
  lazy val maxShields               = baseType.maxShields
  lazy val mineralPrice             = baseType.mineralPrice
  lazy val producesCreep            = baseType.producesCreep
  lazy val producesLarva            = baseType.producesLarva
  lazy val regeneratesHP            = baseType.regeneratesHP
  lazy val requiredTechRaw          = Techs.get(baseType.requiredTech)
  lazy val requiredUnits            = baseType.requiredUnits.asScala.map(pair => (UnitClasses.get(pair._1), pair._2))
  lazy val requiresCreep            = baseType.requiresCreep
  lazy val requiresPsi              = baseType.requiresPsi
  lazy val researchesWhat           = baseType.researchesWhat.asScala.map(Techs.get)
  lazy val seekRange                = baseType.seekRange
  lazy val sightRange               = baseType.sightRange
  lazy val spaceProvided            = baseType.spaceProvided
  lazy val spaceRequired            = baseType.spaceRequired
  lazy val supplyProvided           = baseType.supplyProvided
  lazy val supplyRequired           = baseType.supplyRequired
  lazy val tileHeight               = baseType.tileHeight
  lazy val tileSize                 = new Tile(baseType.tileSize)
  lazy val tileWidth                = baseType.tileWidth
  lazy val topSpeed                 = baseType.topSpeed
  lazy val turnRadius               = baseType.turnRadius
  lazy val upgrades                 = baseType.upgrades.asScala.map(Upgrades.get)
  lazy val upgradesWhat             = baseType.upgradesWhat.asScala.map(Upgrades.get)
  lazy val whatBuilds               = new Pair(UnitClasses.get(baseType.whatBuilds.first), baseType.whatBuilds.second)
  lazy val width                    = baseType.width
  lazy val race                     = baseType.getRace
  lazy val sizeRaw                  = size
  lazy val airWeaponRaw             = baseType.airWeapon
  lazy val groundWeaponRaw          = baseType.groundWeapon
  lazy val airDamageRaw             = baseType.airWeapon.damageAmount
  lazy val airDamageBonusRaw        = baseType.airWeapon.damageBonus
  lazy val airDamageCooldownRaw     = baseType.airWeapon.damageCooldown
  lazy val airDamageFactorRaw       = baseType.airWeapon.damageFactor
  lazy val airDamageTypeRaw         = baseType.airWeapon.damageType
  lazy val airExplosionTypeRaw      = baseType.airWeapon.explosionType
  lazy val airRangeRaw              = baseType.airWeapon.maxRange
  lazy val groundDamageRaw          = baseType.groundWeapon.damageAmount
  lazy val groundDamageBonusRaw     = baseType.groundWeapon.damageBonus
  lazy val groundDamageCooldownRaw  = baseType.groundWeapon.damageCooldown
  lazy val groundDamageFactorRaw    = baseType.groundWeapon.damageFactor
  lazy val groundDamageTypeRaw      = baseType.groundWeapon.damageType
  lazy val groundExplosionTypeRaw   = baseType.groundWeapon.explosionType
  lazy val groundMinRangeRaw        = baseType.groundWeapon.minRange
  lazy val groundRangeRaw           = baseType.groundWeapon.maxRange
  lazy val asString                 = baseType.toString
  
  // .size is broken in BWMirror. This is a manual replacement.
  // Data via http://classic.battle.net/scc/GS/damage.shtml
  lazy val size: UnitSizeType = {
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
    ).contains(baseType))
      UnitSizeType.Small
    else if (Vector(
      UnitType.Terran_Vulture,
      UnitType.Protoss_Corsair,
      UnitType.Zerg_Hydralisk,
      UnitType.Zerg_Defiler,
      UnitType.Zerg_Queen,
      UnitType.Zerg_Lurker
    ).contains(baseType))
      UnitSizeType.Medium
    else
      UnitSizeType.Large
  }
  
  lazy val groundDamageType:DamageType = {
    if (Vector(
      UnitType.Terran_Vulture,
      UnitType.Terran_Ghost,
      UnitType.Terran_Firebat
    ).contains(baseType))
      DamageType.Concussive
    else if (Vector(
      UnitType.Terran_Vulture_Spider_Mine,
      UnitType.Terran_Siege_Tank_Siege_Mode,
      UnitType.Terran_Siege_Tank_Tank_Mode,
      UnitType.Zerg_Hydralisk,
      UnitType.Zerg_Infested_Terran,
      UnitType.Zerg_Sunken_Colony,
      UnitType.Protoss_Dragoon,
      UnitType.Protoss_Arbiter
    ).contains(baseType))
      DamageType.Explosive
    else
      DamageType.Normal
  }
  
  lazy val airDamageType:DamageType = {
    if (Vector(
      UnitType.Terran_Ghost
    ).contains(baseType))
      DamageType.Concussive
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
    ).contains(baseType))
      DamageType.Explosive
    else
      DamageType.Normal
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
