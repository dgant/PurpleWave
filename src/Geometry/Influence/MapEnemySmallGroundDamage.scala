package Geometry.Influence

import bwapi.{UnitType, WeaponType}

class MapEnemySmallGroundDamage extends MapEnemyDamage {
  override def getConcussiveMultiplier:Int = 4
  override def getExplosiveMultiplier:Int = 2
  override def getWeapon(unitType: UnitType):WeaponType = unitType.groundWeapon
}
