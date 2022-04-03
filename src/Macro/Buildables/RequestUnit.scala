package Macro.Buildables

import ProxyBwapi.UnitClasses._

case class RequestUnit(unitClass: UnitClass, quantityArg: Int = 1) extends RequestProduction(unitClass, quantityArg)