package Geometry.Field

import bwapi.{UnitType, WeaponType}

class MapFriendlyGroundDamage extends MapFriendlyDamage {
  override def getConcussiveMultiplier:Int = 3
  override def getExplosiveMultiplier:Int = 3
  override def getWeapon(unitType: UnitType):WeaponType = unitType.groundWeapon
}
