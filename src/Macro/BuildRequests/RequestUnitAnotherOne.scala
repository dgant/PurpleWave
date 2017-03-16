package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClass.UnitClass

case class RequestUnitAnotherOne(unitClass: UnitClass) extends BuildRequest(new BuildableUnit(unitClass)) {
  override def add: Int = 1
}
