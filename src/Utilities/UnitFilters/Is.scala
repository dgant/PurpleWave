package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

case class Is(predicate: UnitInfo => Boolean) extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = predicate(unit)
}
