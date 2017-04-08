package ProxyBwapi

import ProxyBwapi.UnitClass.{UnitClass, UnitTypes}
import bwapi.UnitType

class ProxyBWMirror {
  
  lazy val namesByUnitType:Map[UnitType, String] = UnitTypes.all.map(unitType => (unitType, unitType.toString)).toMap
  lazy val unitClassByTypeName: Map[String, UnitClass] = UnitTypes.all.map(unitType => (unitType.toString, new UnitClass(unitType))).toMap
}
