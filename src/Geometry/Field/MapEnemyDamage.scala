package Geometry.Field


import Startup.With
import bwapi.{Position, UnitType}

abstract class MapEnemyDamage extends MapDamage {
  override def getUnits:Iterable[(Position, UnitType)] = {
    With.units.enemy.filter(_.complete).map(unit => (unit.position, unit.utype))
  }
}
