package ProxyBwapi.Class

import ProxyBwapi.Techs.Techs
import ProxyBwapi.Upgrades.Upgrades
import bwapi.UnitType

import scala.collection.JavaConverters._

case class Clazz(base:UnitType) {
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
  val maxHitPoitns          = base.maxHitPoints
  val maxShields            = base.maxShields
  val mineralPrice          = base.mineralPrice
  val producesCreep         = base.producesCreep
  val producesLarva         = base.producesLarva
  val regeneratesHP         = base.regeneratesHP
  val requiredTech          = Techs.get(base.requiredTech)
  //TODO
  //val requiredUnits = base.requiredUnits.asScala.map(Classes.get)
  val requiresCreep         = base.requiresCreep
  val requiresPsi           = base.requiresPsi
  val researchesWhat        = base.researchesWhat.asScala.map(Techs.get)
  val seekRange             = base.seekRange
  val sightRange            = base.sightRange
  val size                  = base.size
  val spaceProvided         = base.spaceProvided
  val spaceRequired         = base.spaceRequired
  val tileHeight            = base.tileHeight
  val tileSize              = base.tileSize
  val tileWidth             = base.tileWidth
  val topSpeed              = base.topSpeed
  val turnRadius            = base.turnRadius
  val upgrades              = base.upgrades.asScala.map(Upgrades.get)
  val upgradesWhat          = base.upgradesWhat.asScala.map(Upgrades.get)
  //TODO
  //val whatBuilds = base.whatBuilds()
  val width = base.width
  
  
  /*
  not implemented yet
  airWeapon
  getRace
  groundWeapon
   */
}
