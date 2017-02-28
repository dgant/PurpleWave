package Utilities.Enrichment

import bwapi.{DamageType, UnitType}

case object EnrichUnitType {
  implicit class EnrichedUnitType(unitType:UnitType) {
    def groundDamage: Int = {
      val typeMultiplier =
        if (List(DamageType.Concussive, DamageType.Explosive).contains(unitType.groundWeapon.damageType())) {
          .75
        } else {
          1
        }
      val damage = typeMultiplier *
        unitType.maxGroundHits *
        unitType.groundWeapon.damageFactor *
        unitType.groundWeapon.damageAmount
      damage.toInt
    }
    def groundDps: Int = {
      val damagePerSecond = groundDamage * 24 / (2 + unitType.groundWeapon.damageCooldown)
      damagePerSecond.toInt
    }
    
    def range:Int = List(unitType.groundWeapon.maxRange, unitType.airWeapon.maxRange).max
    def totalCost: Int = { unitType.mineralPrice + unitType.gasPrice }
    def isMinerals:Boolean = unitType.isMineralField
    def isGas:Boolean = List(UnitType.Resource_Vespene_Geyser, UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).contains(unitType)
  }
}
