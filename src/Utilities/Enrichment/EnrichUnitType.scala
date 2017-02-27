package Utilities.Enrichment

import bwapi.{DamageType, UnitType}

case object EnrichUnitType {
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
