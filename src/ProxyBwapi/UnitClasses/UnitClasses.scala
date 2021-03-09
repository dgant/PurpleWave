package ProxyBwapi.UnitClasses

import Lifecycle.With
import bwapi.UnitType

object UnitClasses {
  def all: Vector[UnitClass] = With.proxy.unitClasses
  def get(unitType: UnitType): UnitClass = With.proxy.unitClassesById(unitType.id)
  lazy val None: UnitClass = get(UnitType.None)
  lazy val Unknown: UnitClass = get(UnitType.Unknown)
}
