package ProxyBwapi.UnitClasses

import Mathematics.Points.Tile
import ProxyBwapi.{Damage, Size}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi.UnitType

import scala.collection.JavaConverters._

abstract class UnitClassProxy(val bwapiType: UnitType) {
  val id                      = bwapiType.id
  lazy val abilities          = bwapiType.abilities.asScala.map(Techs.get)
  val acceleration256         = bwapiType.acceleration
  val armor                   = bwapiType.armor
  val buildScore              = bwapiType.buildScore
  val buildFrames             = bwapiType.buildTime
  val rawCanAttack            = bwapiType.canAttack
  val canBuildAddon           = bwapiType.canBuildAddon
  val canMove                 = bwapiType.canMove
  val canProduce              = bwapiType.canProduce
  lazy val cloakingTech       = Techs.get(bwapiType.cloakingTech)
  val destroyScore            = bwapiType.destroyScore
  val dimensionDownInclusive  = bwapiType.dimensionDown
  val dimensionDownExclusive = dimensionDownInclusive + 1
  val dimensionLeft           = bwapiType.dimensionLeft
  val dimensionRightInclusive = bwapiType.dimensionRight
  val dimensionRightExclusive = dimensionRightInclusive + 1
  val dimensionUp             = bwapiType.dimensionUp
  val gasPrice                = bwapiType.gasPrice
  val haltPixels              = if (bwapiType == UnitType.Protoss_Dark_Templar) 0 else bwapiType.haltDistance / 256
  val permanentlyCloaked      = bwapiType.hasPermanentCloak
  val height                  = bwapiType.height
  val isAddon                 = bwapiType.isAddon
  val isBeacon                = bwapiType.isBeacon
  val isBuilding              = bwapiType.isBuilding
  val isBurrowable            = bwapiType.isBurrowable
  val isCloakable             = bwapiType.isCloakable
  val isCritter               = bwapiType.isCritter
  val isDetector              = bwapiType.isDetector
  val isFlagBeacon            = bwapiType.isFlagBeacon
  val isFlyer                 = bwapiType.isFlyer
  val isFlyingBuilding        = bwapiType.isFlyingBuilding
  val isHero                  = bwapiType.isHero
  val isInvincible            = bwapiType.isInvincible
  val isMechanical            = bwapiType.isMechanical
  val isMineralField          = bwapiType.isMineralField
  val isNeutral               = bwapiType.isNeutral
  val isOrganic               = bwapiType.isOrganic
  val isPowerup               = bwapiType.isPowerup
  val isRefinery              = bwapiType.isRefinery
  val isResourceContainer     = bwapiType.isResourceContainer
  val isResourceDepot         = bwapiType.isResourceDepot
  val isRobotic               = bwapiType.isRobotic
  val isSpecialBuilding       = bwapiType.isSpecialBuilding
  val isSpell                 = bwapiType.isSpell
  val isSpellcaster           = bwapiType.isSpellcaster
  val isTwoUnitsInOneEgg      = bwapiType.isTwoUnitsInOneEgg
  val isWorker                = bwapiType.isWorker
  val maxAirHitsRaw           = bwapiType.maxAirHits
  val maxEnergy               = bwapiType.maxEnergy
  val maxGroundHitsRaw        = bwapiType.maxGroundHits
  val maxHitPoints            = bwapiType.maxHitPoints
  val maxShields              = bwapiType.maxShields
  val mineralPrice            = bwapiType.mineralPrice
  val producesCreep           = bwapiType.producesCreep
  val producesLarva           = bwapiType.producesLarva
  val regeneratesHP           = bwapiType.regeneratesHP
  lazy val requiredTechRaw    = Techs.get(bwapiType.requiredTech)
  // BWAPI's data includes "whatBuilds" in this this list, but we prefer them separate, partly because it lets us store it as a vector instead of a map
  lazy val requiredUnits      = bwapiType.requiredUnits.asScala.filterNot(_._1 == bwapiType.whatBuilds().getKey).keys.map(UnitClasses.get).toVector
  val requiresCreep           = bwapiType.requiresCreep
  val requiresPsi             = bwapiType.requiresPsi
  lazy val techsWhat          = bwapiType.researchesWhat.asScala.map(Techs.get)
  val seekRange               = bwapiType.seekRange
  val sightPixels             = bwapiType.sightRange
  val spaceProvided           = bwapiType.spaceProvided
  val spaceRequired           = bwapiType.spaceRequired
  val supplyProvided          = bwapiType.supplyProvided
  val supplyRequired          = bwapiType.supplyRequired
  val tileHeight              = bwapiType.tileHeight
  val tileSize                = new Tile(bwapiType.tileSize)
  val tileWidth               = bwapiType.tileWidth
  val topSpeed                = bwapiType.topSpeed
  val turnRadius256           = bwapiType.turnRadius
  lazy val upgrades           = bwapiType.upgrades.asScala.map(Upgrades.get)
  lazy val upgradesWhat       = bwapiType.upgradesWhat.asScala.map(Upgrades.get)
  lazy val whatBuilds         = new Pair(UnitClasses.get(bwapiType.whatBuilds.getKey), bwapiType.whatBuilds.getValue.toInt)
  val width                   = bwapiType.width
  val race                    = bwapiType.getRace
  val airWeaponRaw            = bwapiType.airWeapon
  val groundWeaponRaw         = bwapiType.groundWeapon
  val airDamageRaw            = bwapiType.airWeapon.damageAmount
  val airDamageBonusRaw       = bwapiType.airWeapon.damageBonus
  val airDamageCooldownRaw    = bwapiType.airWeapon.damageCooldown
  val airDamageFactorRaw      = bwapiType.airWeapon.damageFactor
  val airDamageTypeRaw        = bwapiType.airWeapon.damageType
  val airExplosionTypeRaw     = bwapiType.airWeapon.explosionType
  val airRangeRaw             = bwapiType.airWeapon.maxRange
  val airSplashRadius50       = bwapiType.airWeapon.innerSplashRadius
  val airSplashRadius25       = bwapiType.airWeapon.outerSplashRadius
  val groundDamageRaw         = bwapiType.groundWeapon.damageAmount
  val groundDamageBonusRaw    = bwapiType.groundWeapon.damageBonus
  val groundDamageCooldownRaw = bwapiType.groundWeapon.damageCooldown
  val groundDamageFactorRaw   = bwapiType.groundWeapon.damageFactor
  val groundDamageTypeRaw     = bwapiType.groundWeapon.damageType
  val groundExplosionTypeRaw  = bwapiType.groundWeapon.explosionType
  val groundSplashRadius50    = bwapiType.groundWeapon.innerSplashRadius
  val groundSplashRadius25    = bwapiType.groundWeapon.outerSplashRadius
  val groundMinRangeRaw       = bwapiType.groundWeapon.minRange
  val groundRangeRaw          = bwapiType.groundWeapon.maxRange
  val asString                = bwapiType.toString

  // .size is broken in BWMirror. This is a manual replacement.
  // Data via http://classic.battle.net/scc/GS/damage.shtml
  val size: Size.Type = {
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

  val groundDamageType: Damage.Type = {
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

  val airDamageType: Damage.Type = {
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
