package Types.BuildRequest

import Types.Buildable.BuildableUnit
import bwapi.UnitType

case class RequestUnitAtLeast(quantity:Int, unitType: UnitType) extends BuildRequest(new BuildableUnit(unitType)) {
  override def require: Int = quantity
}
