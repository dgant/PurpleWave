package Geometry.Influence

import bwapi.{UnitType, WeaponType}

class MapEnemyMediumAirDamage extends MapEnemyDamage {
  override def getConcussiveMultiplier:Int = 3
  override def getExplosiveMultiplier:Int = 3
  override def getWeapon(unitType: UnitType):WeaponType = unitType.airWeapon
}
