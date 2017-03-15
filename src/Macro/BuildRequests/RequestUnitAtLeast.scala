package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import bwapi.UnitType

case class RequestUnitAtLeast(quantity:Int, unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def require: Int = quantity
}
