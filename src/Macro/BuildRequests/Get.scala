package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClasses.UnitClass

case class Get(quantity: Int, unitClass: UnitClass) extends BuildRequest(BuildableUnit(unitClass)) {
  override def require: Int = quantity
}
