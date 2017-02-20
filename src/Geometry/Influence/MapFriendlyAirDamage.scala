package Geometry.Influence

import bwapi.{UnitType, WeaponType}

class MapFriendlyAirDamage extends MapFriendlyDamage {
  override def getConcussiveMultiplier:Int = 3
  override def getExplosiveMultiplier:Int = 3
  override def getWeapon(unitType: UnitType):WeaponType = unitType.airWeapon
}
