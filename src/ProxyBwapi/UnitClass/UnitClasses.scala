package ProxyBwapi.UnitClass

import bwapi.UnitType

object UnitClasses {
  val get:Map[UnitType, UnitClass] = UnitTypes.all.map(unitType => (unitType, new UnitClass(unitType))).toMap
  val all = get.values.toList
}
