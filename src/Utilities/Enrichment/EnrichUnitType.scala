package Utilities.Enrichment

import Geometry.TileRectangle
import bwapi.{DamageType, TilePosition, UnitType}

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
  
    //Range is from unit edge, so we account for the diagonal width of the unit
    // 7/5 ~= sqrt(2)
    def range:Int = {
      if (unitType == UnitType.Terran_Bunker) { return UnitType.Terran_Marine.range }
      val range = List(unitType.groundWeapon.maxRange, unitType.airWeapon.maxRange).max
      range + unitType.width * 7 / 5
    }
    
    def totalCost: Int = { unitType.mineralPrice + unitType.gasPrice }
    def orderable:Boolean = ! Set(UnitType.Protoss_Interceptor, UnitType.Protoss_Scarab).contains(unitType)
    def isMinerals:Boolean = unitType.isMineralField
    def isGas:Boolean = List(UnitType.Resource_Vespene_Geyser, UnitType.Terran_Refinery, UnitType.Protoss_Assimilator, UnitType.Zerg_Extractor).contains(unitType)
    def isTownHall:Boolean = Set(
      UnitType.Terran_Command_Center,
      UnitType.Protoss_Nexus,
      UnitType.Zerg_Hatchery,
      UnitType.Zerg_Lair,
      UnitType.Zerg_Hive
    ).contains(unitType)
    def area:TileRectangle =
      new TileRectangle(
        new TilePosition(0, 0),
        unitType.tileSize)
    def tiles:Iterable[TilePosition] = area.tiles
  }
}
