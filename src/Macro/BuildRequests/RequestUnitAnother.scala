package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClass.UnitClass

case class RequestUnitAnother(quantity:Int, unit: UnitClass) extends BuildRequest(new BuildableUnit(unit)) {
  override def add: Int = quantity
}
