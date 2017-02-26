package Utilities.Enrichment

import Startup.With
import bwapi.{DamageType, UnitType}

case object EnrichUnit {
  implicit class EnrichedUnit(unit: bwapi.Unit) {
    
    //This ignores spellcasters and workers!
    def canFight: Boolean = {
      unit.isCompleted && unit.getType.canAttack || List(UnitType.Protoss_Carrier, UnitType.Protoss_Reaver).contains(unit.getType)
    }
    
    def attackFrames                    : Int     = { 4 + (if (List(UnitType.Protoss_Dragoon, UnitType.Zerg_Devourer).contains(unit.getType)) 3 else 0) }
    def cooldownRemaining               : Int     = { Math.max(unit.getGroundWeaponCooldown, unit.getAirWeaponCooldown) }
    def stillExists                     : Boolean = { With.unit(unit.getID).nonEmpty }
    def isOurs                          : Boolean = { unit.getPlayer == With.game.self }
    def isFriendly                      : Boolean = { isOurs || unit.getPlayer.isAlly(With.game.self) }
    def isEnemy                         : Boolean = { unit.getPlayer.isEnemy(With.game.self) }
    def totalHealth                     : Int     = { unit.getHitPoints + unit.getShields }
    def initialTotalHealth              : Int     = { unit.getInitialHitPoints + unit.getType.maxShields }
    def range                           : Int     = { List(unit.getType.groundWeapon.maxRange, unit.getType.airWeapon.maxRange).max }
    def isEnemyOf(otherUnit:bwapi.Unit) : Boolean = { unit.getPlayer.isEnemy(otherUnit.getPlayer) }
  
    def groundDps                       : Int     = { unit.getType.groundDps }
    def totalCost                       : Int     = { unit.getType.totalCost }
  }
  
  implicit class EnrichedUnitType(unitType:UnitType) {
    def groundDps: Int = {
      val typeMultiplier =
        if (List(DamageType.Concussive, DamageType.Explosive).contains(unitType.groundWeapon.damageType())) {
          .75
        } else {
          1
        }
      
      val damagePerSecond = typeMultiplier *
        unitType.maxGroundHits *
        unitType.groundWeapon.damageFactor *
        unitType.groundWeapon.damageAmount *
          24 / (2 + unitType.groundWeapon.damageCooldown)
      
      damagePerSecond.toInt
    }
    
    def totalCost           : Int     = { unitType.mineralPrice + unitType.gasPrice }
  }
}
