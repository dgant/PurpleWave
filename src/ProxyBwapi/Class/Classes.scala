package ProxyBwapi.Class

import bwapi.UnitType

object Classes {
  val get:Map[UnitType, Clazz] = UnitTypes.all.map(unitType => (unitType, new Clazz(unitType))).toMap
  val all = get.values.toList
}
