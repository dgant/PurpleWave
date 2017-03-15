package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import bwapi.UnitType

case class RequestUnitAnotherOne(unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def add: Int = 1
}
