package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClass.UnitClass

case class RequestUnitAtLeast(quantity: Int, unitClass: UnitClass) extends BuildRequest(BuildableUnit(unitClass)) {
  override def require: Int = quantity
}
