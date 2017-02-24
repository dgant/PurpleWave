package Utilities.Enrichment

import Startup.With
import bwapi.UnitType

case object EnrichUnit {
  implicit class EnrichedUnit(unit: bwapi.Unit) {
    
    //This ignores spellcasters and workers!
    def canFight: Boolean = {
      unit.isCompleted && (unit.getType.canAttack  && ! unit.getType.isWorker) || List(UnitType.Protoss_Carrier, UnitType.Protoss_Reaver).contains(unit.getType)
    }
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
    //Based on IMP42's estimate of the difference between cooldown and actual frames between attacks
    //To be really accurate, we should add another 3 frames for Dragoons + Devourers
    
    def groundDps           : Int     = { unitType.groundWeapon.damageAmount * 24 / (2 + unitType.groundWeapon.damageCooldown) }
    def totalCost           : Int     = { unitType.mineralPrice + unitType.gasPrice }
  }
}
