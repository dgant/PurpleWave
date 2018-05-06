package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClasses.UnitClass

case class RequestUnitAnotherOne(unitClass: UnitClass) extends BuildRequest(BuildableUnit(unitClass)) {
  override def add: Int = 1
}
