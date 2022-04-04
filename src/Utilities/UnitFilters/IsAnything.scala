package Utilities.UnitFilters
import ProxyBwapi.UnitInfo.UnitInfo

object IsAnything extends UnitFilter {
  override def apply(unit: UnitInfo): Boolean = true
}
