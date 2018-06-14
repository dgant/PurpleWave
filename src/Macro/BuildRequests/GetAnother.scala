package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClasses.UnitClass

case class GetAnother(quantity: Int, unitClass: UnitClass) extends BuildRequest(BuildableUnit(unitClass)) {
  override def add: Int = quantity
}
