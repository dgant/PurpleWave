package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClasses.UnitClass

case class GetAtLeast(quantity: Int, unitClass: UnitClass) extends BuildRequest(BuildableUnit(unitClass)) {
  override def require: Int = quantity
}
