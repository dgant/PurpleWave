package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import bwapi.UnitType

case class RequestUnitAnother(quantity:Int, unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def add: Int = quantity
}
