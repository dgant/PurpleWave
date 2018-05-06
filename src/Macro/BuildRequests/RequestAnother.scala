package Macro.BuildRequests

import Macro.Buildables.BuildableUnit
import ProxyBwapi.UnitClasses.UnitClass

case class RequestAnother(quantity:Int, unit: UnitClass) extends BuildRequest(new BuildableUnit(unit)) {
  override def add: Int = quantity
}
