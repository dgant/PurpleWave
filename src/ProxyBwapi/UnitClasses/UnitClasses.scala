package ProxyBwapi.UnitClasses

import Lifecycle.With
import bwapi.UnitType

object UnitClasses {
  def all: Iterable[UnitClass] = With.proxy.unitClassByName.values
  def None: UnitClass = get(UnitType.None)
  def Unknown: UnitClass = get(UnitType.Unknown)
  
  def get(unitType: UnitType): UnitClass = With.proxy.unitClassByType.getOrElse(unitType, With.proxy.unitClassByName(unitType.toString))
}
