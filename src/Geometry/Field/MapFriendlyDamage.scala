package Geometry.Field

import Startup.With
import bwapi.{Position, UnitType}


abstract class MapFriendlyDamage extends MapDamage {
  override def getUnits:Iterable[(Position, UnitType)] = {
    With.units.ours.map(unit => (unit.position, unit.utype))
  }
}
