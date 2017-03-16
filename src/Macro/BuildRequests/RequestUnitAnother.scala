package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClass.UnitClass
import bwapi.UnitType

case class RequestUnitAnother(quantity:Int, unitType: UnitClass) extends BuildRequest(new BuildableUnit(unitType)) {
  override def add: Int = quantity
}
