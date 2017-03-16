package ProxyBwapi.Class

import bwapi.UnitType

object Classes {
  val get:Map[UnitType, Class] = UnitTypes.all.map(unitType => (unitType, new Class(unitType))).toMap
  val all = get.values.toList
}
