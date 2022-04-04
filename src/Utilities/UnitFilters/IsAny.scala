package Utilities.UnitFilters

import ProxyBwapi.UnitInfo.UnitInfo

case class IsAny(matches: UnitFilter*) extends UnitFilter {
  @inline final override def apply(unit: UnitInfo): Boolean = matches.exists(_(unit))
}
