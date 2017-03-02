package Geometry.Field

import Geometry.Circle
import bwapi.{DamageType, Position, UnitType, WeaponType}

import scala.collection.mutable

abstract class MapDamage extends InfluenceMap {
  
  val _weaponDamage = new mutable.HashMap[UnitType, Int] {
    override def default(unitType:UnitType):Int = {
      val weapon = getWeapon(unitType)
      if (weapon == WeaponType.None || weapon.damageCooldown == 0) { return 0 }
      val multiplier =
        if      (weapon.damageType == DamageType.Concussive) { getConcussiveMultiplier }
        else if (weapon.damageType == DamageType.Explosive)  { getExplosiveMultiplier  }
        else                                                 { getNormalMultiplier     }
      //Doesn't account for upgrades
      multiplier * weapon.damageAmount * weapon.damageFactor * 24 / weapon.damageCooldown
    }
  }
  
  def getConcussiveMultiplier:Int = 4
  def getExplosiveMultiplier:Int = 4
  def getNormalMultiplier:Int = 4
  def getWeapon(unitType: UnitType):WeaponType
  def getUnits:Iterable[(Position, UnitType)]
  
  override def update() {
    reset()
    getUnits.foreach(unit => {
      val position = unit._1
      val unitType = unit._2
      val weaponDamage = _weaponDamage(unitType)
      if (weaponDamage > 0) {
        //Divide into granularity of tiles
        Circle.points((getWeapon(unitType).maxRange + 31) / 32).foreach(point =>
          addPosition(
            position.getX + point._1 * 32,
            position.getY + point._2 * 32,
            weaponDamage))
        if (getWeapon(unitType).minRange > 0) {
          Circle.points((getWeapon(unitType).minRange + 31) / 32).foreach(point =>
            addPosition(
              position.getX + point._1 * 32,
              position.getY + point._2 * 32,
              -weaponDamage))
        }
      }
    })
  }
}
