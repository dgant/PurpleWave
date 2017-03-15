package Types.BuildRequest

import Types.Buildable.BuildableUnit
import bwapi.UnitType

case class RequestUnitAnother(quantity:Int, unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def add: Int = quantity
}
