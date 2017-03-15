package Types.BuildRequest

import Types.Buildable.BuildableUnit
import bwapi.UnitType

case class RequestUnitAnotherOne(unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def add: Int = 1
}
