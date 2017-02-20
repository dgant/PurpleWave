package Geometry.Influence

import bwapi.{UnitType, WeaponType}

class MapEnemyLargeGroundDamage extends MapEnemyDamage {
  override def getConcussiveMultiplier:Int = 2
  override def getExplosiveMultiplier:Int = 4
  override def getWeapon(unitType: UnitType):WeaponType = unitType.groundWeapon
}
