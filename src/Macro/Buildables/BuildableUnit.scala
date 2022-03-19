package Macro.Buildables

import ProxyBwapi.UnitClasses._

case class BuildableUnit(unitClass: UnitClass, quantityArg: Int = 1) extends Buildable(unitClass, quantityArg)